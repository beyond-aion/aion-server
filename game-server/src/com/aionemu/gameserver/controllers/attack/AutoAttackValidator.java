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
 * <p>
 * The rate is checked with a running head start. Every auto attack that arrives earlier than the attack speed allows adds the difference to it, one
 * that arrives later takes it back down (never below zero), and the packet is dropped once the head start passes {@link #MAX_HEAD_START_MILLIS}. Two
 * numbers shape the result: {@link #ALLOWED_OVERSPEED_PERCENT} is the dead band in which rounding and network jitter live, since a client that is at
 * most that much faster than its attack speed never raises the head start at all, and the drop itself spends one interval so a client that never
 * slows down stays at a stable, readable head start instead of one that grows with every packet.
 */
public class AutoAttackValidator {

	/** A client may be this much faster than its attack speed without raising the head start; covers rounding and packet jitter */
	private static final int ALLOWED_OVERSPEED_PERCENT = 1;
	/** How much earlier than its attack speed a client may run in total before its auto attacks are dropped */
	private static final int MAX_HEAD_START_MILLIS = 150;
	/** A streak this long is worth reporting as a rate that the attack speed does not explain, not as an isolated early packet */
	private static final int SUSPICION_DROP_STREAK = 5;
	/** Corridor around the attack animation for weapons with a projectile, whose flight is computed from a position that may be stale */
	private static final int HIT_TIME_TOLERANCE_MILLIS = 200;
	/** Corridor around the attack animation for weapons without a projectile, where only an attack speed the client has not seen yet can move it */
	private static final int MELEE_HIT_TIME_TOLERANCE_MILLIS = 100;
	/** How far outside the corridor a hit time has to be before it is worth an audit entry, for weapons with a projectile */
	private static final int HIT_TIME_AUDIT_THRESHOLD_MILLIS = 500;
	/** The same for weapons without a projectile */
	private static final int MELEE_HIT_TIME_AUDIT_THRESHOLD_MILLIS = 200;

	/**
	 * When the client last announced an auto attack, accepted or not. Measuring from the accepted one only would hand a client that alternates a fast
	 * packet with a dropped one a full interval of credit every time, which is exactly the pattern a raised attack rate produces.
	 */
	private long lastSeenMillis;
	private int lastAttackSpeed;
	private long headStartMillis;
	private long lastIntervalMillis;
	private long shortestIntervalMillis;
	private int totalAutoAttacks;
	private int droppedTotal;
	private int droppedAutoAttacks;
	private boolean attackDelayFlagged;

	/**
	 * @return True if the auto attack may go through. One that may not is to be dropped without a response, since SM_ATTACK_RESPONSE.STOP_* makes the
	 *         client stop attacking altogether instead of skipping one auto attack.
	 */
	public boolean isAllowed(Player player, Creature target) {
		long now = System.currentTimeMillis();
		long interval = takeInterval(now);
		if (isBlockedByLastSkill(player, target, now))
			return false;
		int expectedInterval = expectedInterval(player);
		long headStart = headStartAfter(interval, expectedInterval);
		if (headStart > MAX_HEAD_START_MILLIS) {
			dropTooFast(player, target, interval, expectedInterval, headStart);
			return false;
		}
		accept(interval, headStart, player.getGameStats().getAttackSpeed().getCurrent());
		return true;
	}

	/**
	 * @return Milliseconds since the previous auto attack, accepted or not, or -1 for the first one of the session. The counters live here, so every
	 *         packet a client sends is accounted for, including the ones this validator drops.
	 */
	private long takeInterval(long now) {
		long interval = lastSeenMillis == 0 ? -1 : now - lastSeenMillis;
		lastSeenMillis = now;
		totalAutoAttacks++;
		if (interval > 0 && (shortestIntervalMillis == 0 || interval < shortestIntervalMillis))
			shortestIntervalMillis = interval;
		return interval;
	}

	/**
	 * @return True if the last skill's animation still forbids acting. No tolerance here: the client starts that animation only on
	 *         SM_CASTSPELL_RESULT, so an honest auto attack arrives after this anyway.
	 */
	private boolean isBlockedByLastSkill(Player player, Creature target, long now) {
		long blockedMillis = player.getNextAttackUse() - now;
		if (blockedMillis <= 0)
			return false;
		SkillTemplate lastSkill = player.getLastSkill();
		MotionAudit.recordAutoAttackBeforeSkill(player, blockedMillis, lastSkill == null ? 0 : lastSkill.getSkillId());
		droppedTotal++;
		droppedAutoAttacks++;
		MotionAudit.log(player, "auto attacked " + blockedMillis + " ms before the animation of skill " + (lastSkill == null ? "?" : lastSkill.getSkillId())
			+ " allows it (weapon: " + getWeaponName(player) + ", target: " + target.getName() + ", " + describeRate() + ")");
		return true;
	}

	/**
	 * @return The interval the client is expected to keep. Right after the attack speed changed, by a buff running out or a weapon swap, the client
	 *         may still keep its old pace, so the slower of the two is used.
	 */
	private int expectedInterval(Player player) {
		int attackSpeed = player.getGameStats().getAttackSpeed().getCurrent();
		return lastAttackSpeed == 0 ? attackSpeed : Math.min(attackSpeed, lastAttackSpeed);
	}

	/**
	 * @return The head start this packet leaves behind: how much earlier than the allowed interval it came, on top of what earlier packets left.
	 *         The first auto attack of a session has nothing to be early against, and a late one only pays the head start back.
	 */
	private long headStartAfter(long interval, int expectedInterval) {
		if (interval < 0)
			return 0;
		int shortestAllowedInterval = expectedInterval - expectedInterval * ALLOWED_OVERSPEED_PERCENT / 100;
		return Math.max(0, headStartMillis + shortestAllowedInterval - interval);
	}

	private void dropTooFast(Player player, Creature target, long interval, int expectedInterval, long headStart) {
		MotionAudit.recordAutoAttackTooFast(player, interval, headStart);
		// Spending one interval keeps the head start bounded and readable: a client that never slows down stays blocked, which is the point of the
		// check, but it does so at a stable value instead of one that grows with every packet.
		headStartMillis = Math.clamp(headStart - expectedInterval, 0, MAX_HEAD_START_MILLIS);
		droppedTotal++;
		droppedAutoAttacks++;
		MotionAudit.log(player, "auto attacked after " + interval + " ms at " + player.getGameStats().getAttackSpeed().getCurrent() + " ms attack speed, "
			+ headStart + " ms ahead in total (weapon: " + getWeaponName(player) + ", target: " + target.getName() + ", " + describeRate() + ")");
	}

	private void accept(long interval, long headStart, int attackSpeed) {
		lastIntervalMillis = interval;
		headStartMillis = headStart;
		lastAttackSpeed = attackSpeed;
		droppedAutoAttacks = 0;
	}

	/**
	 * @return The counters that make a raised attack rate readable in one line: how many packets of how many were dropped, the current streak, and
	 *         the shortest interval seen, which is the number an honest client cannot go below for long
	 */
	private String describeRate() {
		StringBuilder sb = new StringBuilder("dropped ").append(droppedTotal).append(" of ").append(totalAutoAttacks).append(", streak ")
			.append(droppedAutoAttacks);
		if (shortestIntervalMillis > 0)
			sb.append(", shortest interval ").append(shortestIntervalMillis).append(" ms");
		if (droppedAutoAttacks == SUSPICION_DROP_STREAK)
			sb.append(", a rate the attack speed does not explain");
		return sb.toString();
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
		lastSeenMillis = 0;
		lastAttackSpeed = 0;
		headStartMillis = 0;
		lastIntervalMillis = 0;
		shortestIntervalMillis = 0;
		totalAutoAttacks = 0;
		droppedTotal = 0;
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
