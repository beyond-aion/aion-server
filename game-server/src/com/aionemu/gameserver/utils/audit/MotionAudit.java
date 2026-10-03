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

	public static void record(Player player, Event event, int a, int b, int c, int d, int e, int f) {
		int flags = 0;
		if (player.getMoveController().isInMove())
			flags |= MotionAuditTrail.MOVING;
		if (player.isInRobotMode())
			flags |= MotionAuditTrail.MECH;
		if (player.isHitTimeBoosted())
			flags |= MotionAuditTrail.HIT_TIME_BOOSTED;
		player.getMotionAuditTrail().add(event, flags, player.getGameStats().getAttackSpeed().getCurrent(), a, b, c, d, e, f);
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
				log.info(player + " " + entry + trail);
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
