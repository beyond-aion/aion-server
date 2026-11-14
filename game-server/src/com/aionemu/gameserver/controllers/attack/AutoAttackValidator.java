package com.aionemu.gameserver.controllers.attack;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.MotionData.AttackHitTimes;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.audit.MotionAudit;

/**
 * Checks the auto attacks a player's client announces: how often they come, and when the client says they land. The client is trusted with neither,
 * but no single late or early packet is punished either, so every decision rests on a tolerance or on a count over several auto attacks.
 */
public class AutoAttackValidator {

	/** How far ahead of its attack speed a client may run in total before its auto attacks are dropped */
	private static final int HEAD_START_TOLERANCE_MILLIS = 150;
	/** Share of an attack interval the head start loses with every accepted auto attack, so a burst after a lag does not linger */
	private static final int HEAD_START_DRAIN_PERCENT = 1;
	/** Corridor around the attack animation for weapons with a projectile, whose flight is computed from a position that may be stale */
	private static final int HIT_TIME_TOLERANCE_MILLIS = 200;
	/** Corridor around the attack animation for weapons without a projectile, where only an attack speed the client has not seen yet can move it */
	private static final int MELEE_HIT_TIME_TOLERANCE_MILLIS = 100;
	/** How far outside the corridor a hit time has to be before it is worth an audit entry, for weapons with a projectile */
	private static final int HIT_TIME_AUDIT_THRESHOLD_MILLIS = 500;
	/** The same for weapons without a projectile */
	private static final int MELEE_HIT_TIME_AUDIT_THRESHOLD_MILLIS = 200;

	private long lastAutoAttackMillis;
	private int lastAttackSpeed;
	private long headStartMillis;
	private long lastIntervalMillis;
	private int droppedAutoAttacks;
	private boolean attackDelayFlagged;

	/**
	 * @return True if the auto attack may go through. One that may not is to be dropped without a response, since SM_ATTACK_RESPONSE.STOP_* makes the
	 *         client stop attacking altogether instead of skipping one auto attack.
	 */
	public boolean isAllowed(Player player, Creature target) {
		long now = System.currentTimeMillis();
		long blockedMillis = player.getNextAttackUse() - now;
		// no tolerance: the client starts the animation only on SM_CASTSPELL_RESULT, so an honest auto attack arrives after this anyway
		if (blockedMillis > 0) {
			SkillTemplate lastSkill = player.getLastSkill();
			MotionAudit.recordAutoAttackBeforeSkill(player, blockedMillis, lastSkill == null ? 0 : lastSkill.getSkillId());
			MotionAudit.log(player, "auto attacked " + blockedMillis + " ms before the animation of skill " + (lastSkill == null ? "?" : lastSkill.getSkillId())
				+ " allows it (weapon: " + getWeaponName(player) + ", target: " + target.getName() + ", " + ++droppedAutoAttacks
				+ " auto attacks dropped in a row)");
			return false;
		}
		int attackSpeed = player.getGameStats().getAttackSpeed().getCurrent();
		// right after the attack speed changed, by a buff running out or a weapon swap, the client may still keep its old pace
		int expectedInterval = lastAttackSpeed == 0 ? attackSpeed : Math.min(attackSpeed, lastAttackSpeed);
		long interval = now - lastAutoAttackMillis;
		// single auto attacks may bunch up within the tolerance, but the average rate has to hold, so the head start accumulates
		// Stricter than retail: the retail server banks lateness up to 2000 ms and allows 200 ms on top, so after a pause or a lag burst several auto
		// attacks may arrive at once. If the audit shows honest players dropped after lag (bunched events), bank lateness up to one attack interval
		// instead of clamping the head start at zero.
		long headStart = Math.max(0, headStartMillis + expectedInterval - interval - expectedInterval * HEAD_START_DRAIN_PERCENT / 100);
		if (headStart > HEAD_START_TOLERANCE_MILLIS) {
			MotionAudit.recordAutoAttackTooFast(player, interval, headStart);
			MotionAudit.log(player, "auto attacked after " + interval + " ms at " + attackSpeed + " ms attack speed, " + headStart
				+ " ms ahead in total (weapon: " + getWeaponName(player) + ", target: " + target.getName() + ", " + ++droppedAutoAttacks
				+ " auto attacks dropped in a row)");
			return false;
		}
		droppedAutoAttacks = 0;
		lastIntervalMillis = lastAutoAttackMillis == 0 ? -1 : interval;
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
		// Stricter than retail: the retail server never compares the auto attack hit time with the animation, it only caps it at 10000 ms
		// (CM_ATTACK does that here too). If the audit shows honest hit times corrected, return the raw value instead.
		int usedHitTime = correctHitTime(player, target, hitTime);
		MotionAudit.recordAutoAttack(player, lastIntervalMillis, headStartMillis, hitTime, usedHitTime, reportedAnimation,
			PositionUtil.getDistance(player, target));
		return usedHitTime;
	}

	private int correctHitTime(Player player, Creature target, int hitTime) {
		if (player.isInRobotMode()) { // a mech auto attack carries no hit time, it hits at once and the client plays out its animation on its own
			if (hitTime != 0)
				MotionAudit.log(player, "sent hit time " + hitTime + " for an auto attack in mech mode, where the client sends none (target: "
					+ target.getName() + ")");
			return 0;
		}
		AttackHitTimes hitTimes = DataManager.MOTION_DATA.calculateAutoAttackHitTimes(player);
		if (hitTimes == null)
			return hitTime;
		double distance = PositionUtil.getDistance(player, target);
		ItemGroup weapon = player.getEquipment().getMainHandWeaponType();
		int ammoSpeed = weapon == null ? 0 : weapon.getAmmoSpeed();
		boolean hasFlightTerm = ammoSpeed != 0;
		int flightMillis = hasFlightTerm ? (int) (1000 * distance / ammoSpeed) : 0;
		int tolerance = hasFlightTerm ? HIT_TIME_TOLERANCE_MILLIS : MELEE_HIT_TIME_TOLERANCE_MILLIS;
		int minHitTime = Math.max(0, hitTimes.lowestMillis() + flightMillis - tolerance);
		int maxHitTime = hitTimes.highestMillis() + flightMillis + tolerance;
		if (hitTime >= minHitTime && hitTime <= maxHitTime)
			return hitTime;
		int correctedHitTime = Math.clamp(hitTime, minHitTime, maxHitTime);
		// the value is corrected either way, an audit entry is only worth it once nothing honest explains the distance
		if (Math.abs(hitTime - correctedHitTime) > (hasFlightTerm ? HIT_TIME_AUDIT_THRESHOLD_MILLIS : MELEE_HIT_TIME_AUDIT_THRESHOLD_MILLIS))
			MotionAudit.log(player, "sent hit time " + hitTime + " for an auto attack, expected " + minHitTime + " to " + maxHitTime + " (weapon: "
				+ getWeaponName(player) + ", target: " + target.getName() + " at " + Math.round(distance) + "m, flight: " + flightMillis + " ms)");
		return correctedHitTime;
	}

	/**
	 * Logs when the client starts flagging its auto attacks for an attack delay that was changed in its memory, once until the flag goes away again.
	 */
	public void checkAttackDelayFlag(Player player, boolean flagged) {
		// Stricter than retail: the retail server ignores this flag. An honest client never sets it, so if the audit shows it on honest players,
		// stop logging it.
		if (flagged && !attackDelayFlagged)
			MotionAudit.log(player, "auto attacked with an attack delay that was changed in the client's memory (weapon: " + getWeaponName(player) + ")");
		attackDelayFlagged = flagged;
	}

	/**
	 * Forgets everything learned about the client's pace, for a player who just entered the world.
	 */
	public void reset() {
		headStartMillis = 0;
		lastAttackSpeed = 0;
		droppedAutoAttacks = 0;
		attackDelayFlagged = false;
	}

	private static String getWeaponName(Player player) {
		ItemGroup weapon = player.getEquipment().getMainHandWeaponType();
		ItemGroup offHand = player.getEquipment().getOffHandWeaponType();
		if (weapon == null)
			return "none";
		return offHand == null ? weapon.name() : weapon.name() + "+" + offHand.name();
	}
}
