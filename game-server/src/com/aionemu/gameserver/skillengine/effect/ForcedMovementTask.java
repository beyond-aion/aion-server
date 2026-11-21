package com.aionemu.gameserver.skillengine.effect;

import java.util.concurrent.ScheduledFuture;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * Moves a creature whose movement is taken over by an effect, like fear or confuse: one step per second.
 */
abstract class ForcedMovementTask implements Runnable {

	private static final int STEP_INTERVAL = 1000;
	/** Each step covers exactly one interval worth of running, so the target arrives just as the next one is issued. */
	private static final float STEP_SECONDS = STEP_INTERVAL / 1000f;
	/** A step which would climb or drop this much or more is discarded. */
	private static final float MAX_STEP_HEIGHT_DIFFERENCE = 4;

	protected final Creature effected;

	protected ForcedMovementTask(Creature effected) {
		this.effected = effected;
	}

	/**
	 * Takes the movement of the effected creature over and schedules the task as the periodic task of the effect. A summon is left where it is: its
	 * position belongs to the client of its master, which neither moves it under these effects nor takes a position from us.
	 */
	static void start(Effect effect, int position, ForcedMovementTask task) {
		Creature effected = task.effected;
		takeOverMovement(effected);
		if (!GeoDataConfig.FEAR_ENABLE || effected instanceof Summon)
			return;
		ScheduledFuture<?> future = ThreadPoolManager.getInstance().scheduleAtFixedRate(task, STEP_INTERVAL, STEP_INTERVAL);
		effect.setPeriodicTask(future, position);
	}

	/**
	 * Stops what the creature was doing before the server took over its movement. The client sends its destination up to a second ahead, so without
	 * this it would keep running there, and the position it reported last must not be used as a starting point for geo lookups anymore, since it stops
	 * reporting for as long as it is not in control.
	 */
	private static void takeOverMovement(Creature effected) {
		effected.getMoveController().abortMove();
		if (effected instanceof Player player)
			player.getMoveController().resetLastPositionFromClient();
	}

	/**
	 * @return True as long as the effect which took over the movement is still on the creature.
	 */
	protected abstract boolean isInControl();

	/**
	 * Called once per step interval, unless the creature must not be moved at all.
	 *
	 * @return True if the creature was sent on its way, false if it stays where it is.
	 */
	protected abstract boolean act();

	@Override
	public void run() {
		if (!isInControl())
			return;
		if (effected.getEffectController().isInAnyAbnormalState(AbnormalState.CANT_BE_FORCE_MOVED))
			return; // another effect is holding or throwing it around, and it knows better than us where it is
		act();
	}

	/**
	 * Moves the creature one interval worth of running, retrying with a fresh deviation whenever it cannot get anywhere.
	 *
	 * @param baseHeading the direction to move into, before any deviation
	 * @param deviations maximum deviation from it in heading units (3° each), one entry per attempt
	 */
	protected boolean step(byte baseHeading, byte[] deviations) {
		float stepDistance = effected.getGameStats().getMovementSpeedFloat() * STEP_SECONDS;
		for (byte deviation : deviations) {
			byte heading = (byte) ((baseHeading + Rnd.get(-deviation, deviation) + 120) % 120);
			Vector3f destination = GeoService.getInstance().findMovementCollision(effected, PositionUtil.convertHeadingToAngle(heading), stepDistance);
			if (!isValidStep(destination, stepDistance))
				continue;
			if (effected instanceof Npc npc) {
				npc.getMoveController().moveToPoint(destination.getX(), destination.getY(), destination.getZ());
			} else {
				effected.getMoveController().setNewDirection(destination.getX(), destination.getY(), destination.getZ(), heading);
				effected.getMoveController().startMovingToDestination();
			}
			return true;
		}
		return false;
	}

	/**
	 * @return True if the creature can actually get to the given point, i.e. it is not blocked right in front of it and the point is not too far above
	 *         or below its current position.
	 */
	private boolean isValidStep(Vector3f destination, float stepDistance) {
		if (!effected.isFlying() && Math.abs(destination.getZ() - effected.getZ()) >= MAX_STEP_HEIGHT_DIFFERENCE)
			return false;
		double distance = PositionUtil.getDistance(effected.getX(), effected.getY(), destination.getX(), destination.getY());
		// Retail discards a step that is blocked anywhere along the way and tries another direction, so the target stops short of an obstacle
		// instead of running into it. Our geo walk stops on every small ledge and on noise in the data, which would freeze targets on rough ground
		// where retail lets them run, so only a step blocked right away is discarded. With retail geo data, require the full step instead:
		// distance >= stepDistance - 0.01f
		return distance >= Math.min(1, stepDistance);
	}
}
