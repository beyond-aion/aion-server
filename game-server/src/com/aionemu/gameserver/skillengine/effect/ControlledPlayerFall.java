package com.aionemu.gameserver.skillengine.effect;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.controllers.movement.MovementMask;
import com.aionemu.gameserver.controllers.movement.PlayableMoveController;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.collision.IgnoreProperties;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOVE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * Drops a player whose movement an effect took over when he ends up in the air, and slides him down ground too steep to stand on, the way his
 * client does. Retail does neither and leaves the server side position where it was while every client moves the character on its own, so nobody
 * can interact with him afterwards. Without this class, that is what we would get back.
 */
final class ControlledPlayerFall {

	enum State {
		/** He stands on the ground and may be stepped. */
		ON_GROUND,
		/** He just reached the ground and may be stepped right away. */
		LANDED,
		/** He just left the ground. */
		FALL_STARTED,
		/** He is in the air or sliding, no step must be taken. */
		MOVING
	}

	static final int TICK_INTERVAL = 50;
	private static final float TICK_SECONDS = TICK_INTERVAL / 1000f;
	/** The slope takes eight lookups to measure, so on ground he stands on it is measured less often than the ground below him is looked for. */
	private static final int TICKS_PER_SLOPE_CHECK = 200 / TICK_INTERVAL;
	/** A player higher above the ground than this is airborne, a hundredth of the height of the collider the client puts around him. */
	private static final float MAX_GROUND_DISTANCE = 0.006f;
	/** Ground up to this far above his feet still is the ground he stands on, the last position his client reported may lie slightly below ours. */
	private static final float MAX_GROUND_ABOVE = 0.5f;
	/** Downward acceleration in units per second², the one a client applies to a character who fell on foot. */
	private static final float GRAVITY = 9.81f;
	/** The client drops a character who just left the air with this instead. */
	private static final float DIVE_GRAVITY = 22;
	/** How far around himself the ground is sampled to find the way down. */
	private static final float SLOPE_PROBE_DISTANCE = 1;
	/** Ground which lies deeper than this next to him is a hole or an edge rather than a slope, and must not be slid onto. */
	private static final float MAX_SLIDE_DROP = 8;
	/** A drop with no ground at all this far below it is a crack in the geo data rather than a cliff, as long as the server is the one moving him. */
	private static final float MIN_SUPPORTED_DROP = 30;
	/** How long after his last own move packet his position counts as one the server walked him to. */
	private static final long CLIENT_POSITION_MILLIS = 1000;

	private final Player player;
	private int ticksUntilSlopeCheck = 1;
	private boolean groundSettled;
	private boolean falling;
	private boolean sliding;
	private float slideSpeed;
	private float gravity;
	private float fallSpeed;

	ControlledPlayerFall(Player player) {
		this.player = player;
	}

	boolean isFalling() {
		return falling;
	}

	/**
	 * Moves him one tick further if he is in the air or on ground too steep to stand on.
	 */
	State update() {
		// an aerial shackle holds him up on purpose, and dropping him would fight it, which looks like a seizure to everyone around
		if (GeoDataConfig.MODE != GeoDataConfig.Mode.ON || player.isFlying() || player.isDead() || !player.isSpawned()
			|| player.getEffectController().isAbnormalSet(AbnormalState.OPENAERIAL))
			return State.ON_GROUND;
		if (falling) // only the ground he actually reaches ends it, checking for nearby ground would leave him hovering above it
			return fall();
		GeoService geo = GeoService.getInstance();
		float groundZ = geo.getZ(player, player.getZ() + MAX_GROUND_ABOVE, player.getZ() - MAX_GROUND_DISTANCE);
		if (!Float.isNaN(groundZ)) {
			standOnGround();
			if (!sliding && --ticksUntilSlopeCheck > 0)
				return State.ON_GROUND;
			ticksUntilSlopeCheck = TICKS_PER_SLOPE_CHECK;
			return slide(groundZ) ? State.MOVING : State.ON_GROUND;
		}
		// the server walked him here, and it walks on ground only: nothing below him at all is a crack in our data, his client stands on solid ground
		boolean walkedByServer = System.currentTimeMillis() - player.getMoveController().getLastPositionFromClientMillis() > CLIENT_POSITION_MILLIS;
		if (walkedByServer && Float.isNaN(geo.getZ(player, player.getZ(), player.getZ() - MIN_SUPPORTED_DROP)))
			return State.ON_GROUND;
		startFalling();
		State state = fall();
		return state == State.MOVING ? State.FALL_STARTED : state;
	}

	/**
	 * Tells everyone around that he is dropping and how fast, so that their clients animate the drop instead of standing him still wherever the last
	 * packet left him.
	 */
	void sendFallArc() {
		PacketSendUtility.broadcastToSightedPlayers(player, new SM_MOVE(player, MovementMask.ARC_MOVE, new Vector3f(0, 0, -fallSpeed)));
	}

	private State fall() {
		float fallDistance = fallSpeed * TICK_SECONDS + gravity * TICK_SECONDS * TICK_SECONDS / 2;
		fallSpeed += gravity * TICK_SECONDS;
		float newZ = player.getZ() - fallDistance;
		float groundZ = GeoService.getInstance().getZ(player.getWorldId(), player.getX(), player.getY(), player.getZ(), newZ, player.getInstanceId());
		if (Float.isNaN(groundZ)) {
			World.getInstance().updatePosition(player, player.getX(), player.getY(), newZ, player.getHeading());
			player.getMoveController().updateFalling(newZ);
			return player.isDead() ? State.ON_GROUND : State.MOVING;
		}
		World.getInstance().updatePosition(player, player.getX(), player.getY(), groundZ, player.getHeading());
		land(groundZ);
		return State.LANDED;
	}

	/**
	 * Slides him down ground he cannot stand on. The client does this on its own, and while it does, the server side position stays up on the slope,
	 * which is the same desync as a target hanging in the air.
	 *
	 * @return True if he is sliding.
	 */
	private boolean slide(float groundZ) {
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
		// between 44° and 46° the client creeps down at about a tenth of a unit per second, which is not worth following
		if (slope <= PlayableMoveController.MAX_STANDING_SLOPE)
			return stopSliding(); // on ground he can stand on the client brakes him to a halt within a frame
		if (!sliding) {
			sliding = true;
			slideSpeed = 0;
			stopStep(); // the step which is still running would fight the slide for the position
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
	private void stopStep() {
		player.getMoveController().abortMove();
		PacketSendUtility.sendPacket(player, new SM_MOVE(player));
	}

	private void startFalling() {
		falling = true;
		stopSliding();
		stopStep(); // the step which is still running would fight the fall for the position
		gravity = player.getMoveController().isDivingFromFlight() ? DIVE_GRAVITY : GRAVITY;
		// the client drops a character it just lost control over straight down from a standstill, whatever it was doing a moment earlier
		fallSpeed = 0;
		sendFallArc(); // everyone else only ever hears about the drop from us
	}

	private void land(float groundZ) {
		falling = false;
		fallSpeed = 0;
		player.getMoveController().stopFalling(groundZ);
		player.getController().updateZone(); // he may have landed in water or anywhere else the zones care about
	}

	/**
	 * Keeps the start of the next fall at the ground he stands on, since the client reports no position while the server moves him. The first time
	 * this also settles a fall which his client had begun before it lost control, and which no move packet can end anymore.
	 */
	private void standOnGround() {
		if (groundSettled) {
			player.getMoveController().restartFalling(player.getZ());
		} else {
			groundSettled = true;
			player.getMoveController().stopFalling(player.getZ());
		}
	}
}
