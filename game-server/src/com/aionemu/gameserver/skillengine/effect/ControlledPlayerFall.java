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
	/** The slope and an edge take several lookups to check, so they are checked less often than the ground below him is looked for. */
	private static final int TICKS_PER_SLOPE_CHECK = 200 / TICK_INTERVAL;
	/** A player higher above the ground than this is airborne, a hundredth of the height of the collider the client puts around him. */
	private static final float MAX_GROUND_DISTANCE = 0.006f;
	/** Ground up to this far above his feet still is the ground he stands on, the last position his client reported may lie slightly below ours. */
	private static final float MAX_GROUND_ABOVE = 0.5f;
	/** Downward acceleration in units per second², the one a client applies to a character who fell on foot. */
	private static final float GRAVITY = 9.81f;
	/** The client drops a character who just left the air with this instead. */
	private static final float DIVE_GRAVITY = 22;
	/** Ground steeper than this (its tangent), 44°, no longer holds him still: the client lets him creep down it until it gets steeper than 46°. */
	private static final float MIN_CREEP_SLOPE = (float) Math.tan(Math.toRadians(44));
	/**
	 * How strongly the client pulls a character on the ground towards the speed it wants, per second. Against gravity along the slope this leaves one
	 * who wants to stand creeping down at g * sin / 46, about a tenth of a unit per second.
	 */
	private static final float GROUND_INERTIA = 46;
	/**
	 * How far from his feet the ground is sampled to tell how steep it is and which way it falls. The client takes the face he stands on, so the
	 * samples stay close enough to land on it too.
	 */
	private static final float SLOPE_PROBE_DISTANCE = 0.1f;
	/** A sample farther above or below him than this hit a ledge or a wall next to him rather than the ground under his feet. */
	private static final float SLOPE_PROBE_HEIGHT = 0.5f;
	/** Ground which lies deeper than this next to him is a hole or an edge rather than a slope, and must not be slid onto. */
	private static final float MAX_SLIDE_DROP = 8;
	/** A drop with no ground at all this far below it is a crack in the geo data rather than a cliff, as long as the server is the one moving him. */
	private static final float MIN_SUPPORTED_DROP = 30;
	/** Radius of the cylinder the client puts around him. */
	private static final float COLLIDER_RADIUS = 0.4f;
	/**
	 * How far above his feet the bottom of that cylinder is, which is also the highest ledge he walks up. Over an edge the cylinder sits down on it
	 * with his feet this far below, and holds him there until his middle is farther out than the radius.
	 */
	private static final float COLLIDER_BOTTOM = 0.5f;
	/** How far apart in height the edge and the bottom of the cylinder may be when checking whether he still hangs on it. */
	private static final float LEDGE_TOLERANCE = 0.05f;
	/** How long after his last own move packet his position counts as one the server walked him to. */
	private static final long CLIENT_POSITION_MILLIS = 1000;

	private final Player player;
	private int ticksUntilSlopeCheck = 1;
	private boolean groundSettled;
	private boolean falling;
	private boolean sliding;
	private boolean creeping;
	/** He hangs on an edge by the bottom of the cylinder, with nothing under his feet. */
	private boolean hanging;
	/** The fall started from a standstill, so he can come to rest on the edge he left, and where it started. */
	private boolean mayHang;
	private float fallTopZ;
	/** Tangent of the ground under him and the horizontal unit vector pointing down it, as of the last check. */
	private float slope, downhillX, downhillY;
	private float slideSpeed;
	private float gravity;
	private float fallSpeed;

	ControlledPlayerFall(Player player) {
		this.player = player;
	}

	/**
	 * Puts him where the jump he is in has carried him by now. The client reports the start of a jump only, and drops the character from wherever the
	 * jump has taken it when it loses control, so without this the fall would start back at the takeoff.
	 */
	void catchUpWithJump() {
		Vector3f jump = player.getMoveController().getJumpPosition();
		player.getMoveController().onJumpEnded();
		if (jump == null || GeoDataConfig.MODE != GeoDataConfig.Mode.ON || player.isFlying())
			return;
		GeoService geo = GeoService.getInstance();
		Vector3f reached = geo.getClosestCollision(player, jump.getX(), jump.getY(), jump.getZ(), false, CollisionIntention.DEFAULT_COLLISIONS.getId(),
			IgnoreProperties.ANY_RACE);
		// the arc may already have come down to the ground, or below it
		float groundZ = geo.getZ(player.getWorldId(), reached.getX(), reached.getY(), Math.max(reached.getZ(), player.getZ()) + MAX_GROUND_ABOVE,
			reached.getZ(), player.getInstanceId());
		float z = Float.isNaN(groundZ) ? reached.getZ() : groundZ;
		World.getInstance().updatePosition(player, reached.getX(), reached.getY(), z, player.getHeading());
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
			hanging = false;
			if (!sliding && !creeping && --ticksUntilSlopeCheck > 0) {
				standOnGround();
				return State.ON_GROUND;
			}
			ticksUntilSlopeCheck = TICKS_PER_SLOPE_CHECK;
			measureSlope(groundZ);
			if (slope > PlayableMoveController.MAX_STANDING_SLOPE)
				return slide(groundZ);
			if (sliding) { // the ground got flat enough to stand on, so the slide ends like a fall does
				stopSliding();
				land(groundZ);
				return State.LANDED;
			}
			standOnGround();
			creep(groundZ);
			return State.ON_GROUND;
		}
		boolean stepping = player.getMoveController().isMovingToDestination();
		if (hanging && !stepping) {
			if (--ticksUntilSlopeCheck > 0)
				return State.ON_GROUND;
			ticksUntilSlopeCheck = TICKS_PER_SLOPE_CHECK;
			float bottomZ = player.getZ() + COLLIDER_BOTTOM;
			if (!Float.isNaN(findLedge(bottomZ + LEDGE_TOLERANCE, bottomZ - LEDGE_TOLERANCE))) {
				standOnGround();
				return State.ON_GROUND;
			}
		}
		hanging = false;
		// the server walked him here, and it walks on ground only: nothing below him at all is a crack in our data, his client stands on solid ground
		boolean walkedByServer = System.currentTimeMillis() - player.getMoveController().getLastPositionFromClientMillis() > CLIENT_POSITION_MILLIS;
		if (walkedByServer && Float.isNaN(geo.getZ(player, player.getZ(), player.getZ() - MIN_SUPPORTED_DROP)))
			return State.ON_GROUND;
		// one who runs off an edge flies on past it, one who stands still or creeps drops onto it with the cylinder
		startFalling(!stepping);
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
		// below the edge he left, the cylinder has nothing left to sit down on
		if (mayHang && fallTopZ - player.getZ() < 2 * COLLIDER_BOTTOM) {
			float ledgeZ = findLedge(player.getZ() + COLLIDER_BOTTOM, newZ + COLLIDER_BOTTOM);
			if (!Float.isNaN(ledgeZ) && (Float.isNaN(groundZ) || ledgeZ - COLLIDER_BOTTOM > groundZ)) {
				float z = ledgeZ - COLLIDER_BOTTOM;
				World.getInstance().updatePosition(player, player.getX(), player.getY(), z, player.getHeading());
				hanging = true;
				ticksUntilSlopeCheck = TICKS_PER_SLOPE_CHECK;
				land(z);
				return State.LANDED;
			}
		}
		if (Float.isNaN(groundZ)) {
			World.getInstance().updatePosition(player, player.getX(), player.getY(), newZ, player.getHeading());
			player.getMoveController().updateFalling(newZ);
			return player.isDead() ? State.ON_GROUND : State.MOVING;
		}
		World.getInstance().updatePosition(player, player.getX(), player.getY(), groundZ, player.getHeading());
		measureSlope(groundZ);
		if (slope > PlayableMoveController.MAX_STANDING_SLOPE) { // the client does not stand him on ground this steep, the fall goes on down it
			double inclination = Math.atan(slope);
			falling = false;
			sliding = true;
			slideSpeed = fallSpeed * (float) Math.sin(inclination); // what is left of the speed once the ground took the part pressing into it
			fallSpeed = 0;
			player.getController().updateZone();
			return State.MOVING;
		}
		land(groundZ);
		return State.LANDED;
	}

	/**
	 * Looks for an edge the bottom of his cylinder rests on: ground within its radius, between the given heights.
	 *
	 * @return The height of the highest such ground, or {@link Float#NaN} if there is none.
	 */
	private float findLedge(float zMax, float zMin) {
		GeoService geo = GeoService.getInstance();
		float ledgeZ = Float.NaN;
		for (float distance = COLLIDER_RADIUS / 2; distance <= COLLIDER_RADIUS; distance += COLLIDER_RADIUS / 2) {
			for (int i = 0; i < 8; i++) {
				double angle = i * Math.PI / 4;
				float x = player.getX() + (float) Math.cos(angle) * distance;
				float y = player.getY() + (float) Math.sin(angle) * distance;
				float z = geo.getZ(player.getWorldId(), x, y, zMax, zMin, player.getInstanceId());
				if (!Float.isNaN(z) && (Float.isNaN(ledgeZ) || z > ledgeZ))
					ledgeZ = z;
			}
		}
		return ledgeZ;
	}

	/**
	 * Takes the slope of the ground under him from four samples around his feet. Where a sample finds no ground nearby, the one on the other side
	 * stands in for it.
	 */
	private void measureSlope(float groundZ) {
		float riseX = getRise(groundZ, 1, 0);
		float riseY = getRise(groundZ, 0, 1);
		slope = (float) Math.sqrt(riseX * riseX + riseY * riseY);
		downhillX = slope == 0 ? 0 : -riseX / slope;
		downhillY = slope == 0 ? 0 : -riseY / slope;
	}

	/**
	 * @return How much the ground rises per unit along the given axis, 0 if no sample found ground.
	 */
	private float getRise(float groundZ, int axisX, int axisY) {
		float ahead = getProbeZ(groundZ, axisX, axisY);
		float behind = getProbeZ(groundZ, -axisX, -axisY);
		if (!Float.isNaN(ahead) && !Float.isNaN(behind))
			return (ahead - behind) / (2 * SLOPE_PROBE_DISTANCE);
		if (!Float.isNaN(ahead))
			return (ahead - groundZ) / SLOPE_PROBE_DISTANCE;
		if (!Float.isNaN(behind))
			return (groundZ - behind) / SLOPE_PROBE_DISTANCE;
		return 0;
	}

	private float getProbeZ(float groundZ, int axisX, int axisY) {
		float x = player.getX() + axisX * SLOPE_PROBE_DISTANCE;
		float y = player.getY() + axisY * SLOPE_PROBE_DISTANCE;
		return GeoService.getInstance().getZ(player.getWorldId(), x, y, groundZ + SLOPE_PROBE_HEIGHT, groundZ - SLOPE_PROBE_HEIGHT, player.getInstanceId());
	}

	/**
	 * Slides him down ground he cannot stand on. The client does this on its own, and while it does, the server side position stays up on the slope,
	 * which is the same desync as a target hanging in the air. To the client he is in the air all the way down, so the slide counts as a fall, and the
	 * height it ends below where he started falling is what hurts him.
	 */
	private State slide(float groundZ) {
		if (!sliding) {
			sliding = true;
			creeping = false;
			slideSpeed = 0;
			if (player.getMoveController().isMovingToDestination())
				stopStep(); // the step which is still running would fight the slide for the position
		}
		// frictionless along the slope: gravity accelerates him by g * sin, and of the speed along the slope cos is horizontal
		double inclination = Math.atan(slope);
		slideSpeed += GRAVITY * (float) Math.sin(inclination) * TICK_SECONDS;
		float distance = slideSpeed * (float) Math.cos(inclination) * TICK_SECONDS;
		Move move = moveDownhill(groundZ, distance);
		float edgeX = player.getX() + downhillX * distance, edgeY = player.getY() + downhillY * distance, edgeZ = player.getZ() - distance * slope;
		// nothing at all below the edge is a crack in our data rather than a cliff, and he must not fall through the world
		if (move == Move.NO_GROUND && Float.isNaN(GeoService.getInstance().getZ(player.getWorldId(), edgeX, edgeY, edgeZ, edgeZ - MIN_SUPPORTED_DROP,
			player.getInstanceId())))
			move = Move.BLOCKED;
		switch (move) {
			case BLOCKED -> { // he comes to rest against whatever stopped him
				stopSliding();
				land(player.getZ());
				return State.LANDED;
			}
			case NO_GROUND -> { // the ground drops away under him, and he goes on falling from the edge
				World.getInstance().updatePosition(player, edgeX, edgeY, edgeZ, player.getHeading());
				fallSpeed = slideSpeed * (float) Math.sin(inclination);
				stopSliding();
				falling = true;
				mayHang = false; // he slides off with speed, past the edge
				gravity = GRAVITY;
				sendFallArc();
				return State.FALL_STARTED;
			}
		}
		return State.MOVING;
	}

	/**
	 * Lets him creep down ground between 44° and 46° while no step moves him. The client does not hold a character still there: what it wants to stand
	 * loses against gravity along the slope, so he creeps until the ground gets steep enough to slide down or flat enough to stand on.
	 */
	private void creep(float groundZ) {
		if (slope <= MIN_CREEP_SLOPE || player.getMoveController().isMovingToDestination()) {
			creeping = false;
			return;
		}
		double inclination = Math.atan(slope);
		float speed = GRAVITY * (float) Math.sin(inclination) / GROUND_INERTIA;
		creeping = moveDownhill(groundZ, speed * (float) Math.cos(inclination) * TICK_SECONDS) == Move.MOVED;
	}

	private enum Move {
		MOVED,
		BLOCKED,
		NO_GROUND
	}

	/**
	 * Moves him the given horizontal distance down the slope and onto the ground there, unless something blocks the way or there is no ground to put
	 * him on.
	 */
	private Move moveDownhill(float groundZ, float distance) {
		GeoService geo = GeoService.getInstance();
		float newX = player.getX() + downhillX * distance;
		float newY = player.getY() + downhillY * distance;
		Vector3f blocked = geo.getClosestCollision(player, newX, newY, player.getZ(), false, CollisionIntention.DEFAULT_COLLISIONS.getId(),
			IgnoreProperties.ANY_RACE);
		if (blocked.getX() != newX || blocked.getY() != newY)
			return Move.BLOCKED;
		float newZ = geo.getZ(player.getWorldId(), newX, newY, groundZ + 1, groundZ - MAX_SLIDE_DROP, player.getInstanceId());
		if (Float.isNaN(newZ))
			return Move.NO_GROUND;
		World.getInstance().updatePosition(player, newX, newY, newZ, player.getHeading());
		player.getController().updateZone();
		return Move.MOVED;
	}

	private void stopSliding() {
		sliding = false;
		slideSpeed = 0;
	}

	/**
	 * Stops the step he is taking, for himself as well: the stop which goes to everyone else does not reach his own client, which would otherwise walk
	 * the step out first.
	 */
	private void stopStep() {
		player.getMoveController().abortMove();
		PacketSendUtility.sendPacket(player, new SM_MOVE(player));
	}

	private void startFalling(boolean mayHang) {
		falling = true;
		this.mayHang = mayHang;
		fallTopZ = player.getZ();
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
