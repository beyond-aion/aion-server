package com.aionemu.gameserver.utils.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.LoggingConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.utils.audit.MotionAuditTrail.Event;

/**
 * Audit entries of the motion gates. They are heuristics that a lagging honest client can trip, so they never punish, and each comes with the state
 * of the player and, at most every {@link #TRAIL_DUMP_INTERVAL_MILLIS}, the events that led to it in a separate log.
 */
public class MotionAudit {

	private static final Logger log = LoggerFactory.getLogger("MOTION_AUDIT_LOG");
	private static final long TRAIL_DUMP_INTERVAL_MILLIS = 10000;
	/** A jump this recent is named in an entry, since landing cuts the animation and the client then sends its next action early */
	private static final long RECENT_JUMP_MILLIS = 3000;

	public static void recordCast(Player player, int skillId, int castMillis, int clientHitTime, int hitTime, long nextSkillInMillis,
		long nextAutoAttackInMillis) {
		record(player, Event.CAST, skillId, castMillis, clientHitTime, hitTime, toInt(nextSkillInMillis), toInt(nextAutoAttackInMillis));
	}

	public static void recordSkillTooEarly(Player player, int skillId, long tooEarlyMillis, int previousSkillId) {
		record(player, Event.SKILL_TOO_EARLY, skillId, toInt(tooEarlyMillis), previousSkillId, 0, 0, 0);
	}

	/**
	 * @param intervalMillis
	 *          Time since the previous auto attack, -1 for the first one
	 * @param animation
	 *          The number of the attack animation the client says it played, or 0 if unknown
	 */
	public static void recordAutoAttack(Player player, long intervalMillis, long headStartMillis, int clientHitTime, int hitTime, int animation,
		double distance) {
		record(player, Event.AUTO_ATTACK, toInt(intervalMillis), toInt(headStartMillis), clientHitTime, hitTime, animation,
			(int) Math.round(distance * 10));
	}

	public static void recordAutoAttackBeforeSkill(Player player, long tooEarlyMillis, int lastSkillId) {
		record(player, Event.AUTO_ATTACK_BEFORE_SKILL, toInt(tooEarlyMillis), lastSkillId, 0, 0, 0, 0);
	}

	public static void recordAutoAttackTooFast(Player player, long intervalMillis, long headStartMillis) {
		record(player, Event.AUTO_ATTACK_TOO_FAST, toInt(intervalMillis), toInt(headStartMillis), 0, 0, 0, 0);
	}

	/**
	 * @param hitTime
	 *          The hit time the damage was scheduled with, or 0 if the attack was dropped
	 */
	public static void recordSummonAttack(Player master, long intervalMillis, int clientHitTime, int hitTime, boolean accepted) {
		record(master, Event.SUMMON_ATTACK, toInt(intervalMillis), clientHitTime, hitTime, accepted ? 1 : 0, 0, 0);
	}

	/**
	 * The values go in the order of {@link Event}'s value names, unused ones are 0.
	 */
	private static void record(Player player, Event event, int value1, int value2, int value3, int value4, int value5, int value6) {
		int flags = 0;
		if (player.getMoveController().isInMove())
			flags |= MotionAuditTrail.MOVING;
		if (player.isInRobotMode())
			flags |= MotionAuditTrail.MECH;
		if (player.isHitTimeBoosted())
			flags |= MotionAuditTrail.HIT_TIME_BOOSTED;
		player.getMotionAuditTrail().add(event, flags, player.getGameStats().getAttackSpeed().getCurrent(), value1, value2, value3, value4, value5,
			value6);
	}

	private static int toInt(long millis) {
		return (int) Math.max(Integer.MIN_VALUE, Math.min(millis, Integer.MAX_VALUE));
	}

	/**
	 * Records a jump or a landing, only for players whose trail exists already, so jumping around in town costs nothing
	 */
	public static void recordMovement(Player player, Event event) {
		MotionAuditTrail trail = player.findMotionAuditTrail();
		if (trail != null)
			trail.add(event, 0, player.getGameStats().getAttackSpeed().getCurrent(), 0, 0, 0, 0, 0, 0);
	}

	public static void log(Player player, String message) {
		String entry = message + " [" + describeState(player) + "]";
		AuditLogger.log(player, entry, false);
		if (LoggingConfig.LOG_AUDIT) {
			String trail = player.getMotionAuditTrail().dump(TRAIL_DUMP_INTERVAL_MILLIS);
			if (trail != null)
				log.info("{} {}{}", player, entry, trail);
		}
	}

	private static String describeState(Player player) {
		MotionAuditTrail trail = player.findMotionAuditTrail();
		String recentJump = trail == null ? null : trail.describeRecentJump(RECENT_JUMP_MILLIS);
		return (recentJump == null ? "" : recentJump + ", ") + player.getRace() + " " + player.getGender() + ", weapons " + player.getEquipment().getMainHandWeaponType() + "/"
			+ player.getEquipment().getOffHandWeaponType() + ", attack speed " + player.getGameStats().getAttackSpeed().getCurrent() + " ms (base "
			+ player.getGameStats().getAttackSpeed().getBase() + "), casting speed "
			+ Math.round(player.getGameStats().getReverseStat(StatEnum.BOOST_CASTING_TIME, 1000).getCurrent() / 10f) / 100f
			+ (player.isHitTimeBoosted() ? ", hit time boosted" : "") + (player.getMoveController().isInMove() ? ", moving" : "")
			+ (player.isInRobotMode() ? ", mech" : "");
	}
}
