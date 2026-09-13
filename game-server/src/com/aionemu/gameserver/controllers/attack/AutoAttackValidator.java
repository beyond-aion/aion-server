package com.aionemu.gameserver.controllers.attack;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.MotionData.AttackHitTimes;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * Checks the auto attacks a player's client announces: how often they come, and when the client says they land. The client is trusted with neither,
 * but no single late or early packet is punished either, so every decision rests on a tolerance or on a count over several auto attacks.
 */
public class AutoAttackValidator {

	/** How far ahead of its attack speed a client may run in total before its auto attacks are dropped */
	private static final int HEAD_START_TOLERANCE_MILLIS = 150;
	/** Share of an attack interval the head start loses with every accepted auto attack, so a burst after a lag does not linger */
	private static final int HEAD_START_DRAIN_PERCENT = 1;
	/** How long before the animation of the last cast allows it an auto attack may still arrive */
	private static final int AFTER_CAST_TOLERANCE_MILLIS = 150;
	/** Corridor around the attack animation for weapons with a projectile, whose flight is computed from a position that may be stale */
	private static final int HIT_TIME_TOLERANCE_MILLIS = 200;
	/** Corridor around the attack animation for weapons without a projectile, where only an attack speed the client has not seen yet can move it */
	private static final int MELEE_HIT_TIME_TOLERANCE_MILLIS = 100;
	/** How far outside the corridor a hit time has to be before it is worth an audit entry, for weapons with a projectile */
	private static final int HIT_TIME_AUDIT_THRESHOLD_MILLIS = 500;
	/** The same for weapons without a projectile */
	private static final int MELEE_HIT_TIME_AUDIT_THRESHOLD_MILLIS = 200;
	/** The two attack animations of a weapon must be this far apart to tell which one a hit time belongs to */
	private static final int ANIMATION_SPREAD_MILLIS = 50;
	/** Auto attacks to judge the mix of animations over */
	private static final int ANIMATION_SAMPLE_SIZE = 60;
	/** Share of a single animation within the sample that no honest client reaches, since it draws one at random per auto attack */
	private static final int ANIMATION_SHARE_PERCENT = 95;

	private long lastAutoAttackMillis;
	private int lastAttackSpeed;
	private long headStartMillis;
	private int droppedAutoAttacks;
	private int shortAnimationAutoAttacks;
	private int longAnimationAutoAttacks;
	private int lastShortAnimationMillis;

	/**
	 * @return True if the auto attack may go through. One that may not is to be dropped without a response, since SM_ATTACK_RESPONSE.STOP_* makes the
	 *         client stop attacking altogether instead of skipping one auto attack.
	 */
	public boolean isAllowed(Player player, Creature target) {
		long now = System.currentTimeMillis();
		long blockedMillis = player.getNextAttackUse() - now;
		if (blockedMillis > AFTER_CAST_TOLERANCE_MILLIS) {
			SkillTemplate lastSkill = player.getLastSkill();
			AuditLogger.log(player, "auto attacked " + blockedMillis + " ms before the animation of skill " + (lastSkill == null ? "?" : lastSkill.getSkillId())
				+ " allows it (weapon: " + getWeaponName(player) + ", target: " + target.getName() + ", " + ++droppedAutoAttacks
				+ " auto attacks dropped in a row)");
			return false;
		}
		int attackSpeed = player.getGameStats().getAttackSpeed().getCurrent();
		// right after the attack speed changed, by a buff running out or a weapon swap, the client may still keep its old pace
		int expectedInterval = lastAttackSpeed == 0 ? attackSpeed : Math.min(attackSpeed, lastAttackSpeed);
		long interval = now - lastAutoAttackMillis;
		// single auto attacks may bunch up within the tolerance, but the average rate has to hold, so the head start accumulates
		long headStart = Math.max(0, headStartMillis + expectedInterval - interval - expectedInterval * HEAD_START_DRAIN_PERCENT / 100);
		if (headStart > HEAD_START_TOLERANCE_MILLIS) {
			AuditLogger.log(player, "auto attacked after " + interval + " ms at " + attackSpeed + " ms attack speed, " + headStart
				+ " ms ahead in total (weapon: " + getWeaponName(player) + ", target: " + target.getName() + ", " + ++droppedAutoAttacks
				+ " auto attacks dropped in a row)");
			return false;
		}
		droppedAutoAttacks = 0;
		headStartMillis = headStart;
		lastAutoAttackMillis = now;
		lastAttackSpeed = attackSpeed;
		return true;
	}

	/**
	 * Limits the hit time to what the attack animation can produce, as it schedules the damage and a modified client can send anything.
	 *
	 * @param reportedAnimation
	 *          The number of the attack animation the client says it played, or 0 if unknown
	 */
	public int validateHitTime(Player player, Creature target, int hitTime, int reportedAnimation) {
		if (player.isInRobotMode()) { // a mech auto attack carries no hit time, it hits at once and the client plays out its animation on its own
			if (hitTime != 0)
				AuditLogger.log(player, "sent hit time " + hitTime + " for an auto attack in mech mode, where the client sends none (target: "
					+ target.getName() + ")");
			return 0;
		}
		AttackHitTimes hitTimes = DataManager.MOTION_DATA.calculateAutoAttackHitTimes(player);
		if (hitTimes == null) // no attack animation for the equipped weapon
			return hitTime;
		double distance = PositionUtil.getDistance(player, target);
		ItemGroup weapon = player.getEquipment().getMainHandWeaponType();
		int ammoSpeed = weapon == null ? 0 : weapon.getAmmoSpeed();
		boolean hasFlightTerm = ammoSpeed != 0;
		int flightMillis = hasFlightTerm ? (int) (1000 * distance / ammoSpeed) : 0;
		int tolerance = hasFlightTerm ? HIT_TIME_TOLERANCE_MILLIS : MELEE_HIT_TIME_TOLERANCE_MILLIS;
		int minHitTime = Math.max(0, hitTimes.lowestMillis() + flightMillis - tolerance);
		int maxHitTime = hitTimes.highestMillis() + flightMillis + tolerance;
		// both run on every auto attack, a client that keeps to one animation stays inside the corridor
		int reportedAnimationMillis = hitTime - flightMillis;
		countAnimation(player, hitTimes, reportedAnimationMillis);
		checkReportedAnimation(player, target, hitTimes, reportedAnimationMillis, reportedAnimation, tolerance);
		if (hitTime >= minHitTime && hitTime <= maxHitTime)
			return hitTime;
		int correctedHitTime = Math.clamp(hitTime, minHitTime, maxHitTime);
		// the value is corrected either way, an audit entry is only worth it once nothing honest explains the distance
		if (Math.abs(hitTime - correctedHitTime) > (hasFlightTerm ? HIT_TIME_AUDIT_THRESHOLD_MILLIS : MELEE_HIT_TIME_AUDIT_THRESHOLD_MILLIS))
			AuditLogger.log(player, "sent hit time " + hitTime + " for an auto attack, expected " + minHitTime + " to " + maxHitTime + " (weapon: "
				+ getWeaponName(player) + ", target: " + target.getName() + " at " + Math.round(distance) + "m, flight: " + flightMillis + " ms)");
		return correctedHitTime;
	}

	/**
	 * Forgets everything learned about the client's pace, for a player who just entered the world.
	 */
	public void reset() {
		headStartMillis = 0;
		lastAttackSpeed = 0;
		droppedAutoAttacks = 0;
		resetAnimationMix();
	}

	/**
	 * Compares the animation the client named with the one its hit time belongs to. An honest client takes both from the clip it is playing, so they
	 * cannot disagree.
	 */
	private void checkReportedAnimation(Player player, Creature target, AttackHitTimes hitTimes, int reportedAnimationMillis, int reportedAnimation,
		int tolerance) {
		if (reportedAnimation != 1 && reportedAnimation != 2)
			return;
		if (hitTimes.highestMillis() - hitTimes.lowestMillis() < ANIMATION_SPREAD_MILLIS)
			return;
		int namedMillis = hitTimes.millisFor(reportedAnimation);
		if (Math.abs(reportedAnimationMillis - namedMillis) <= tolerance)
			return;
		AuditLogger.log(player, "auto attacked with animation " + reportedAnimation + ", which hits at " + namedMillis + " ms, but reported a hit time of "
			+ reportedAnimationMillis + " ms (weapon: " + getWeaponName(player) + ", target: " + target.getName() + ")");
	}

	/**
	 * Counts which of the two attack animations the reported hit time belongs to. The client draws the animation anew for every auto attack, so a player
	 * whose auto attacks all claim the same one is reporting what he likes instead of what he plays.
	 */
	private void countAnimation(Player player, AttackHitTimes hitTimes, int reportedAnimationMillis) {
		if (hitTimes.highestMillis() - hitTimes.lowestMillis() < ANIMATION_SPREAD_MILLIS)
			return;
		if (hitTimes.lowestMillis() != lastShortAnimationMillis) { // another weapon or another attack speed, the counts cannot be compared
			resetAnimationMix();
			lastShortAnimationMillis = hitTimes.lowestMillis();
		}
		if (Math.abs(reportedAnimationMillis - hitTimes.lowestMillis()) <= Math.abs(reportedAnimationMillis - hitTimes.highestMillis()))
			shortAnimationAutoAttacks++;
		else
			longAnimationAutoAttacks++;
		int autoAttacks = shortAnimationAutoAttacks + longAnimationAutoAttacks;
		if (autoAttacks < ANIMATION_SAMPLE_SIZE)
			return;
		int oneSided = Math.max(shortAnimationAutoAttacks, longAnimationAutoAttacks);
		if (oneSided * 100 / autoAttacks >= ANIMATION_SHARE_PERCENT)
			AuditLogger.log(player, "auto attacked with the " + (shortAnimationAutoAttacks > longAnimationAutoAttacks ? "short" : "long") + " animation "
				+ oneSided + " of " + autoAttacks + " times, although the client picks one at random per auto attack (weapon: " + getWeaponName(player)
				+ ", the two animations hit at " + hitTimes.lowestMillis() + " and " + hitTimes.highestMillis() + " ms)");
		resetAnimationMix();
	}

	private void resetAnimationMix() {
		shortAnimationAutoAttacks = 0;
		longAnimationAutoAttacks = 0;
	}

	private static String getWeaponName(Player player) {
		ItemGroup weapon = player.getEquipment().getMainHandWeaponType();
		ItemGroup offHand = player.getEquipment().getOffHandWeaponType();
		if (weapon == null)
			return "none";
		return offHand == null ? weapon.name() : weapon.name() + "+" + offHand.name();
	}
}
