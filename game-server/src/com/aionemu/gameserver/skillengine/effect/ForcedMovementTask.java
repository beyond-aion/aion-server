package com.aionemu.gameserver.skillengine.effect;

import java.util.concurrent.ScheduledFuture;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.controllers.movement.MovementMask;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOVE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * Moves a creature whose movement is taken over by an effect, like fear or confuse, one step at a time. A player is also dropped or slid down by
 * {@link ControlledPlayerFall} between the steps.
 */
abstract class ForcedMovementTask implements Runnable {

	/** A player takes one step per second. */
	private static final int PLAYER_STEP_INTERVAL = 1000;
	/** An npc takes one step per two thirds of a second, so it arrives just as the next one is issued and never stops in between. */
	private static final int NPC_STEP_INTERVAL = 667;
	/** A fall changes the position on every tick, so the onlookers hear about it far more often than about a target which stays put. */
	private static final int TICKS_PER_FALL_UPDATE = 200 / ControlledPlayerFall.TICK_INTERVAL;
	/** A step which would climb or drop this much or more is discarded. */
	private static final float MAX_STEP_HEIGHT_DIFFERENCE = 4;

	protected final Creature effected;
	/** Only a player can be left in the air or on a slope, the server walks everyone else along ground it found itself. */
	private final ControlledPlayerFall fall;
	private final int stepInterval;
	/** A player is ticked often enough for a smooth fall, everyone else only when the next step is due. */
	private final int tickInterval;
	private final int ticksPerStep;
	private int ticksUntilNextStep;
	private int ticksUntilResync;

	protected ForcedMovementTask(Creature effected) {
		this.effected = effected;
		fall = effected instanceof Player player ? new ControlledPlayerFall(player) : null;
		stepInterval = fall != null ? PLAYER_STEP_INTERVAL : NPC_STEP_INTERVAL;
		tickInterval = fall != null ? ControlledPlayerFall.TICK_INTERVAL : NPC_STEP_INTERVAL;
		ticksPerStep = stepInterval / tickInterval;
		ticksUntilNextStep = ticksPerStep;
		ticksUntilResync = ticksPerStep;
	}

	/**
	 * Takes the movement of the effected creature over and schedules the task as the periodic task of the effect. A summon is left where it is: its
	 * position belongs to the client of its master, which neither moves it under these effects nor takes a position from us.
	 */
	static void start(Effect effect, int position, ForcedMovementTask task) {
		Creature effected = task.effected;
		if (task.fall != null)
			task.fall.catchUpWithJump(); // before the stop goes out, so that it already names where he is
		takeOverMovement(effected);
		if (!GeoDataConfig.FEAR_ENABLE || effected instanceof Summon)
			return;
		ScheduledFuture<?> future = ThreadPoolManager.getInstance().scheduleAtFixedRate(task, task.tickInterval, task.tickInterval);
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
	 * Called once per step interval, unless the creature is in the air or must not be moved at all.
	 *
	 * @return True if the creature was sent on its way, false if it stays where it is.
	 */
	protected abstract boolean act();

	@Override
	public void run() {
		if (!isInControl())
			return;
		// the interval runs on no matter what the target does, so a landing does not trigger a step of its own
		boolean isStepDue = --ticksUntilNextStep <= 0;
		if (isStepDue)
			ticksUntilNextStep = ticksPerStep;
		if (fall != null) { // none of the conditions below hold a fall back
			switch (fall.update()) {
				case FALL_STARTED -> {
					ticksUntilResync = TICKS_PER_FALL_UPDATE;
					return;
				}
				case MOVING -> {
					if (--ticksUntilResync <= 0)
						resync(false);
					return;
				}
				case LANDED -> ticksUntilNextStep = 1; // he carries on right away, his client is done landing by the time the packet reaches it
				case ON_GROUND -> {
					// both clients creep him down on their own, his own included, and only the server can keep them together
					if (fall.isCreeping() && --ticksUntilResync <= 0)
						resync(true);
				}
			}
		}
		if (!isStepDue)
			return;
		if (effected.getEffectController().isInAnyAbnormalState(AbnormalState.CANT_BE_FORCE_MOVED))
			return; // another effect is holding or throwing it around, and it knows better than us where it is
		if (!act() && fall != null) // an npc is stopped by its own movement once it arrived
			holdPosition((Player) effected);
	}

	/**
	 * Moves the creature one interval worth of running, retrying with a fresh deviation whenever it cannot get anywhere.
	 *
	 * @param baseHeading the direction to move into, before any deviation
	 * @param deviations maximum deviation from it in heading units (3° each), one entry per attempt
	 */
	protected boolean step(byte baseHeading, byte[] deviations) {
		// exactly one interval worth of running, so the creature arrives just as the next step is issued
		float stepDistance = effected.getGameStats().getMovementSpeedFloat() * stepInterval / 1000f;
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
			ticksUntilResync = ticksPerStep;
			return true;
		}
		return false;
	}

	/**
	 * Sends the position to everyone around, so clients which extrapolated a slide or a fall of their own snap back onto what the server believes.
	 * Retail sends nothing at all in these moments, which is what makes a target that got stuck impossible to hit for everyone else.
	 *
	 * @param toSelf whether his own client gets it too, which only makes sense where it moves him the same way as the server does
	 */
	private void resync(boolean toSelf) {
		if (fall.isFalling()) {
			ticksUntilResync = TICKS_PER_FALL_UPDATE;
			fall.sendFallArc();
			return;
		}
		ticksUntilResync = ticksPerStep;
		PacketSendUtility.broadcastToSightedPlayers(effected, new SM_MOVE(effected, MovementMask.IMMEDIATE), toSelf);
	}

	/**
	 * Ends the step he arrived at and tells everyone, himself included, where the server has him. While no step can be found, his own client gets no
	 * position from anywhere else and may creep or slide away on its own, so it would show him somewhere else than everyone else sees him.
	 */
	private void holdPosition(Player player) {
		ticksUntilResync = ticksPerStep;
		player.getMoveController().abortMove(); // tells everyone else
		PacketSendUtility.sendPacket(player, new SM_MOVE(player));
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
