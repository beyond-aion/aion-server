package com.aionemu.gameserver.skillengine.effect;

import java.util.concurrent.ScheduledFuture;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.controllers.movement.MovementMask;
import com.aionemu.gameserver.controllers.movement.PlayerMoveController;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.IgnoreProperties;
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
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * Moves a creature whose movement is taken over by an effect, like fear or confuse: one step per second. A player who ends up in the air or on
 * ground too steep to stand on is also dropped or slid down by the server. Retail does neither and leaves the server side position where it was,
 * while every client moves the character on its own, so nobody can interact with him afterwards. Without the fall and slide here, that is what
 * we would get back.
 */
abstract class ForcedMovementTask implements Runnable {

	private static final int STEP_INTERVAL = 1000;
	/** Players are checked this often, since they may have to be dropped or slid smoothly. Everyone else only ever takes steps. */
	private static final int PLAYER_TICK_INTERVAL = 50;
	private static final int TICKS_PER_GROUND_CHECK = 200 / PLAYER_TICK_INTERVAL;
	/** A fall changes the position on every tick, so the onlookers hear about it far more often than about a target which stays put. */
	private static final int TICKS_PER_FALL_UPDATE = 200 / PLAYER_TICK_INTERVAL;
	private static final float TICK_SECONDS = PLAYER_TICK_INTERVAL / 1000f;
	/** Each step covers exactly one interval worth of running, so the target arrives just as the next one is issued. */
	private static final float STEP_SECONDS = STEP_INTERVAL / 1000f;
	/** A step which would climb or drop this much or more is discarded. */
	private static final float MAX_STEP_HEIGHT_DIFFERENCE = 4;
	/**
	 * A player higher above the ground than this is airborne. The client already drops him a hundredth of a unit above it, this leaves room for our geo
	 * data and for the straight line a step takes over uneven ground.
	 */
	private static final float MAX_GROUND_DISTANCE = 1.5f;
	/** Downward acceleration in units per second², the one a client applies to a character who fell on foot. */
	private static final float GRAVITY = 9.81f;
	/** The client drops a character who just left the air with this instead. */
	private static final float DIVE_GRAVITY = 22;
	/**
	 * Ground steeper than 46° (its tangent) cannot be stood on: the client lets go of the character there, which then slides down without any friction.
	 * Between 44° and 46° the client creeps down at about a tenth of a unit per second, which is not worth following.
	 */
	private static final float MIN_SLIDE_SLOPE = (float) Math.tan(Math.toRadians(46));
	/** How far around himself the ground is sampled to find the way down. */
	private static final float SLOPE_PROBE_DISTANCE = 1;
	/** Ground which lies deeper than this next to him is a hole or an edge rather than a slope, and must not be slid onto. */
	private static final float MAX_SLIDE_DROP = 8;
	/** A drop with no ground at all this far below it is a crack in the geo data rather than a cliff, as long as the server is the one moving him. */
	private static final float MIN_SUPPORTED_DROP = 30;

	protected final Creature effected;
	private final int ticksPerStep;
	private int ticksUntilNextStep;
	private int ticksUntilGroundCheck = 1;
	private int ticksUntilResync;
	private boolean groundSettled;
	private boolean falling;
	private boolean sliding;
	private float slideSpeed;
	private float gravity;
	private float fallSpeed;

	protected ForcedMovementTask(Creature effected) {
		this.effected = effected;
		ticksPerStep = STEP_INTERVAL / getTickInterval(effected);
		ticksUntilNextStep = ticksPerStep;
		ticksUntilResync = ticksPerStep;
	}

	private static int getTickInterval(Creature effected) {
		return effected instanceof Player ? PLAYER_TICK_INTERVAL : STEP_INTERVAL;
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
		int tickInterval = getTickInterval(effected);
		ScheduledFuture<?> future = ThreadPoolManager.getInstance().scheduleAtFixedRate(task, tickInterval, tickInterval);
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
		if (effected instanceof Player player && isGroundCheckDue() && isAirborneOrSliding(player)) { // none of the conditions below hold a fall back
			if (--ticksUntilResync <= 0)
				resync();
			return;
		}
		if (!isStepDue)
			return;
		if (effected.getEffectController().isInAnyAbnormalState(AbnormalState.CANT_BE_FORCE_MOVED))
			return; // another effect is holding or throwing it around, and it knows better than us where it is
		if (!act())
			resync();
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
			ticksUntilResync = ticksPerStep;
			return true;
		}
		return false;
	}

	/**
	 * Sends the position to everyone around, so clients which extrapolated a slide or a fall of their own snap back onto what the server believes.
	 * Retail sends nothing at all in these moments, which is what makes a target that got stuck impossible to hit for everyone else.
	 */
	private void resync() {
		if (falling && effected instanceof Player player) {
			ticksUntilResync = TICKS_PER_FALL_UPDATE;
			sendFallArc(player);
			return;
		}
		ticksUntilResync = ticksPerStep;
		PacketSendUtility.broadcastToSightedPlayers(effected, new SM_MOVE(effected, MovementMask.IMMEDIATE));
	}

	/**
	 * Tells everyone around that the player is dropping and how fast, so that their clients animate the drop instead of standing him still wherever
	 * the last packet left him.
	 */
	private void sendFallArc(Player player) {
		PlayerMoveController moveController = player.getMoveController();
		moveController.vectorX = 0;
		moveController.vectorY = 0;
		moveController.vectorZ = -fallSpeed;
		PacketSendUtility.broadcastToSightedPlayers(player, new SM_MOVE(player, MovementMask.ARC_MOVE));
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

	/**
	 * @return True if the ground below the player must be looked for. While he is in the air or sliding this is the case on every tick, since his
	 *         position changes with it, on the ground it is enough to look for it now and then.
	 */
	private boolean isGroundCheckDue() {
		if (falling || sliding)
			return true;
		if (--ticksUntilGroundCheck > 0)
			return false;
		ticksUntilGroundCheck = TICKS_PER_GROUND_CHECK;
		return true;
	}

	/**
	 * Drops a player who is in the air towards the ground, or slides him down ground he cannot stand on.
	 *
	 * @return True if the player is still in the air or sliding, i.e. no step must be taken.
	 */
	private boolean isAirborneOrSliding(Player player) {
		// an aerial shackle holds him up on purpose, and dropping him would fight it, which looks like a seizure to everyone around
		if (GeoDataConfig.MODE != GeoDataConfig.Mode.ON || player.isFlying() || player.isDead() || !player.isSpawned()
			|| player.getEffectController().isAbnormalSet(AbnormalState.OPENAERIAL))
			return false;
		GeoService geo = GeoService.getInstance();
		if (!falling) { // once he is falling only the ground he actually reaches ends it, checking for nearby ground would leave him hovering above it
			float groundZ = geo.getZ(player, player.getZ() + 0.5f, player.getZ() - MAX_GROUND_DISTANCE);
			if (!Float.isNaN(groundZ)) {
				standOnGround(player);
				return slide(player, groundZ);
			}
			// the server walked him here, and it walks on ground only: nothing below him at all is a crack in our data, his client stands on solid ground
			boolean walkedByServer = System.currentTimeMillis() - player.getMoveController().getLastPositionFromClientMillis() > STEP_INTERVAL;
			if (walkedByServer && Float.isNaN(geo.getZ(player, player.getZ(), player.getZ() - MIN_SUPPORTED_DROP)))
				return false;
			startFalling(player);
		}
		return fall(player);
	}

	/**
	 * @return True if the player is still in the air.
	 */
	private boolean fall(Player player) {
		float fallDistance = fallSpeed * TICK_SECONDS + gravity * TICK_SECONDS * TICK_SECONDS / 2;
		fallSpeed += gravity * TICK_SECONDS;
		float newZ = player.getZ() - fallDistance;
		float groundZ = GeoService.getInstance().getZ(player.getWorldId(), player.getX(), player.getY(), player.getZ(), newZ, player.getInstanceId());
		if (Float.isNaN(groundZ)) {
			World.getInstance().updatePosition(player, player.getX(), player.getY(), newZ, player.getHeading());
			player.getMoveController().updateFalling(newZ);
			return !player.isDead();
		}
		World.getInstance().updatePosition(player, player.getX(), player.getY(), groundZ, player.getHeading());
		land(player, groundZ);
		return false;
	}

	/**
	 * Slides a player down ground he cannot stand on. The client does this on its own, and while it does, the server side position stays up on the
	 * slope, which is the same desync as a target hanging in the air.
	 *
	 * @return True if the player is sliding, i.e. no step must be taken.
	 */
	private boolean slide(Player player, float groundZ) {
		GeoService geo = GeoService.getInstance();
		float lowestZ = groundZ;
		double downhillAngle = 0;
		for (byte heading = 0; heading < 120; heading += 15) { // eight directions around him, the way down is the lowest of them
			double angle = Math.toRadians(PositionUtil.convertHeadingToAngle(heading));
			float x = player.getX() + (float) Math.cos(angle) * SLOPE_PROBE_DISTANCE;
			float y = player.getY() + (float) Math.sin(angle) * SLOPE_PROBE_DISTANCE;
			float z = geo.getZ(player.getWorldId(), x, y, groundZ + 1, groundZ - MAX_SLIDE_DROP, player.getInstanceId());
			if (!Float.isNaN(z) && z < lowestZ) {
				lowestZ = z;
				downhillAngle = angle;
			}
		}
		float slope = (groundZ - lowestZ) / SLOPE_PROBE_DISTANCE;
		if (slope <= MIN_SLIDE_SLOPE)
			return stopSliding(); // on ground he can stand on the client brakes him to a halt within a frame
		if (!sliding) {
			sliding = true;
			slideSpeed = 0;
			stopStep(player); // the step which is still running would fight the slide for the position
		}
		// frictionless along the slope: gravity accelerates him by g * sin, and of the speed along the slope cos is horizontal
		double inclination = Math.atan(slope);
		slideSpeed += GRAVITY * (float) Math.sin(inclination) * TICK_SECONDS;
		float distance = slideSpeed * (float) Math.cos(inclination) * TICK_SECONDS;
		float newX = player.getX() + (float) Math.cos(downhillAngle) * distance;
		float newY = player.getY() + (float) Math.sin(downhillAngle) * distance;
		Vector3f blocked = geo.getClosestCollision(player, newX, newY, player.getZ(), false, CollisionIntention.DEFAULT_COLLISIONS.getId(),
			IgnoreProperties.ANY_RACE);
		if (blocked.getX() != newX || blocked.getY() != newY) // ran into something on the way down, so it stops here
			return stopSliding();
		float newZ = geo.getZ(player.getWorldId(), newX, newY, groundZ + 1, groundZ - MAX_SLIDE_DROP, player.getInstanceId());
		if (Float.isNaN(newZ)) // the ground ends or drops away here, so he stops at the edge and the fall takes over if there is nothing below him
			return stopSliding();
		World.getInstance().updatePosition(player, newX, newY, newZ, player.getHeading());
		player.getMoveController().restartFalling(newZ);
		player.getController().updateZone();
		return true;
	}

	private boolean stopSliding() {
		sliding = false;
		slideSpeed = 0;
		return false;
	}

	/**
	 * Stops the step he is taking, for himself as well: the stop which goes to everyone else does not reach his own client, which would otherwise walk
	 * the step out first.
	 */
	private static void stopStep(Player player) {
		player.getMoveController().abortMove();
		PacketSendUtility.sendPacket(player, new SM_MOVE(player));
	}

	private void startFalling(Player player) {
		falling = true;
		stopSliding();
		stopStep(player); // the step which is still running would fight the fall for the position
		gravity = player.getMoveController().isDivingFromFlight() ? DIVE_GRAVITY : GRAVITY;
		// the client drops a character it just lost control over straight down from a standstill, whatever it was doing a moment earlier
		fallSpeed = 0;
		ticksUntilResync = TICKS_PER_FALL_UPDATE;
		sendFallArc(player); // everyone else only ever hears about the drop from us
	}

	private void land(Player player, float groundZ) {
		falling = false;
		fallSpeed = 0;
		player.getMoveController().stopFalling(groundZ);
		player.getController().updateZone(); // he may have landed in water or anywhere else the zones care about
		ticksUntilNextStep = 1; // he carries on right away, his client is done landing by the time the packet reaches it
	}

	/**
	 * Keeps the start of the next fall at the ground he stands on, since the client reports no position while the server moves him. The first time
	 * this also settles a fall which his client had begun before it lost control, and which no move packet can end anymore.
	 */
	private void standOnGround(Player player) {
		if (groundSettled) {
			player.getMoveController().restartFalling(player.getZ());
		} else {
			groundSettled = true;
			player.getMoveController().stopFalling(player.getZ());
		}
	}
}
