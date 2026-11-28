package com.aionemu.gameserver.controllers.movement;

import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.stats.StatFunctions;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 */
public class PlayerMoveController extends PlayableMoveController<Player> {

	/**
	 * Heights in meters: no damage up to the first, death from the second on. A fall in mid air with no ground near enough below is lethal past
	 * the third, or the fourth in fly zones.
	 */
	private static final float MIN_FALL_DAMAGE_HEIGHT = 10, LETHAL_FALL_HEIGHT = 50, MAX_MIDAIR_FALL_HEIGHT = 50, MAX_MIDAIR_FALL_HEIGHT_IN_FLY_ZONE = 100;
	/** The client flies a jump with twice the gravity of a fall on foot. */
	private static final float JUMP_GRAVITY = 19.62f;
	/** No jump lasts longer than this, a jump which seems to is one whose landing went unreported. */
	private static final float MAX_JUMP_SECONDS = 3;

	private float fallStartZ = Float.NaN;
	private float lastZ = Float.NaN;
	private byte lastMovementMask;
	private long lastPositionFromClientMillis;
	private WorldPosition lastPositionFromClient;
	private long lastRandomMoveLocEffectTimeMillis;
	private boolean divingFromFlight;
	private long jumpStartMillis;
	private float jumpStartX, jumpStartY, jumpStartZ, jumpVelocityX, jumpVelocityY, jumpVelocityZ;

	public PlayerMoveController(Player owner) {
		super(owner);
	}

	public byte getLastMovementMask() {
		return lastMovementMask;
	}

	public long getLastPositionFromClientMillis() {
		return lastPositionFromClientMillis;
	}

	public WorldPosition getLastPositionFromClient() {
		return lastPositionFromClient;
	}

	public void resetLastPositionFromClient() {
		lastPositionFromClient = null;
	}

	/**
	 * This method should only be called from player move packets, not any calculated intermediate position updates by the server
	 */
	public void onMoveFromClient() {
		updateLastMove();
		lastMovementMask = getMovementMask();
		lastPositionFromClientMillis = System.currentTimeMillis();
		if (lastPositionFromClient == null || lastPositionFromClient.getMapId() != owner.getWorldId())
			lastPositionFromClient = new WorldPosition(owner.getWorldId(), owner.getX(), owner.getY(), owner.getZ(), owner.getHeading());
		else
			lastPositionFromClient.setXYZH(owner.getX(), owner.getY(), owner.getZ(), owner.getHeading());
		updateMovementModifiers();
	}

	private void updateMovementModifiers() {
		switch (getMoveRequest(getMovementMask())) {
			case DIRECTION -> updateMovementModifierDirection();
			case POINT -> setMovementModifierDirection(MovementModifierDirection.FORWARD);
			case STOP -> onMovementStopped();
		}
	}

	/**
	 * Only packets with both POSITION and MANUAL carry a new movement request. All others keep the current direction as long as the movement goes on
	 * (POSITION) or the player is airborne (FALL), so neither turning in mid air nor a plain position update restarts the activation timer.
	 */
	static MoveRequest getMoveRequest(byte movementMask) {
		if ((movementMask & MovementMask.POSITION) == MovementMask.POSITION && (movementMask & MovementMask.MANUAL) == MovementMask.MANUAL)
			return (movementMask & MovementMask.ABSOLUTE) == MovementMask.ABSOLUTE ? MoveRequest.POINT : MoveRequest.DIRECTION;
		if ((movementMask & MovementMask.POSITION) == 0 && (movementMask & MovementMask.FALL) == 0)
			return MoveRequest.STOP;
		return MoveRequest.CONTINUE;
	}

	enum MoveRequest {
		/** Movement along a vector, classified relative to the own heading. */
		DIRECTION,
		/** Movement towards a clicked point, which always counts as forward. */
		POINT,
		CONTINUE,
		STOP
	}

	public void resetToLastPositionFromClient() {
		abortMove();
		if (lastPositionFromClient != null && owner.getWorldId() == lastPositionFromClient.getMapId())
			owner.getPosition().setXYZH(lastPositionFromClient.getX(), lastPositionFromClient.getY(), lastPositionFromClient.getZ(),
					lastPositionFromClient.getHeading());
	}

	/**
	 * Marks the player as left in the air after flying or gliding: his client drops him more than twice as fast as one who fell on foot, until he
	 * lands.
	 */
	public void setDivingFromFlight() {
		divingFromFlight = true;
	}

	public boolean isDivingFromFlight() {
		return divingFromFlight;
	}

	/**
	 * Called for move packets in which the player falls. A fall packet that already touches the ground counts as landing.
	 */
	public void updateFalling(float newZ) {
		if (!Float.isNaN(GeoService.getInstance().getZ(owner, newZ + 0.3f, newZ - 0.3f))) {
			stopFalling(newZ);
			return;
		}
		if (isBelowWorld(newZ)) {
			dieFromFall();
			return;
		}
		if (!Float.isNaN(fallStartZ) && newZ < lastZ) {
			float height = fallStartZ - newZ;
			float maxHeight = owner.isInsideFlyZone() ? MAX_MIDAIR_FALL_HEIGHT_IN_FLY_ZONE : MAX_MIDAIR_FALL_HEIGHT;
			if (height > maxHeight && Float.isNaN(GeoService.getInstance().getWalkableZ(owner, newZ, newZ - 2 * maxHeight))) {
				dieFromFall();
				return;
			}
		}
		lastZ = newZ;
		owner.getObserveController().notifyMoveObservers();
	}

	/**
	 * Called for all other move packets: the player stands on the ground, flies or glides.
	 */
	public void stopFalling(float newZ) {
		divingFromFlight = false;
		if (isBelowWorld(newZ)) {
			dieFromFall();
			return;
		}
		float height = fallStartZ - newZ;
		if (!owner.isFlying() && height > MIN_FALL_DAMAGE_HEIGHT) {
			if (height >= LETHAL_FALL_HEIGHT) {
				dieFromFall();
				return;
			}
			if (!owner.isInSprintMode()) {
				owner.getLifeStats().reduceHp(TYPE.FALL_DAMAGE, StatFunctions.calculateFallDamage(owner, height), 0, LOG.REGULAR, owner);
				owner.getObserveController().notifyAttackedObservers(owner, 0);
			}
		}
		fallStartZ = newZ;
		lastZ = newZ;
		owner.getObserveController().notifyMoveObservers();
	}

	/**
	 * Lets the next fall start at the given height, as after leaving a windstream or landing from free flight.
	 */
	public void restartFalling(float z) {
		fallStartZ = z;
		lastZ = z;
	}

	/**
	 * Remembers where and how fast a jump started: the client reports no position until it comes down again.
	 */
	public void onJumpStarted(float x, float y, float z, float velocityX, float velocityY, float velocityZ) {
		jumpStartMillis = System.currentTimeMillis();
		jumpStartX = x;
		jumpStartY = y;
		jumpStartZ = z;
		jumpVelocityX = velocityX;
		jumpVelocityY = velocityY;
		jumpVelocityZ = velocityZ;
	}

	public void onJumpEnded() {
		jumpStartMillis = 0;
	}

	/**
	 * @return Where the jump he started last has carried him by now, ignoring whatever it ran into, or null if he is not jumping.
	 */
	public Vector3f getJumpPosition() {
		if (jumpStartMillis == 0)
			return null;
		float seconds = (System.currentTimeMillis() - jumpStartMillis) / 1000f;
		if (seconds > MAX_JUMP_SECONDS)
			return null;
		return new Vector3f(jumpStartX + jumpVelocityX * seconds, jumpStartY + jumpVelocityY * seconds,
			jumpStartZ + jumpVelocityZ * seconds - JUMP_GRAVITY * seconds * seconds / 2);
	}

	/**
	 * Forgets where the current fall started, so a teleport or a landing from a flight path doesn't count as one.
	 */
	public void resetFalling() {
		onJumpEnded();
		divingFromFlight = false;
		fallStartZ = Float.NaN;
		lastZ = Float.NaN;
	}

	/**
	 * @return True if the player is more than 2 m below z 0.
	 */
	private static boolean isBelowWorld(float z) {
		return z < -2;
	}

	/**
	 * A lethal fall shows no resurrection options, except in instances that ask for them: the player is revived a second later in the instance, at
	 * an active kisk or at the bind point. A revive already scheduled on death, like the one of PvP zones, is kept.
	 */
	private void dieFromFall() {
		resetFalling();
		if (owner.isDead())
			return;
		if (owner.getController().die(TYPE.FALL_DAMAGE, LOG.REGULAR, owner)
			&& !owner.getWorldMapInstance().getInstanceHandler().showResurrectionOptionsAfterLethalFall()
			&& !owner.getController().hasScheduledTask(TaskId.TELEPORT))
			PlayerReviveService.scheduleReviveAtBase(owner, 1000, 0);
	}

	public void setHasMovedByRandomMoveLocEffect(Skill skill) {
		// delayMillis is required because instant skills (like Power: Emergency Teleport I) are not scheduled with hitTime in endCast
		int delayMillis = skill.isInstantSkill() ? skill.getHitTime() : 0;
		this.lastRandomMoveLocEffectTimeMillis = System.currentTimeMillis() + delayMillis;
	}

	public boolean hasMovedByRandomMoveLocEffect() {
		return lastRandomMoveLocEffectTimeMillis != 0 && System.currentTimeMillis() - lastRandomMoveLocEffectTimeMillis < 300;
	}
}
