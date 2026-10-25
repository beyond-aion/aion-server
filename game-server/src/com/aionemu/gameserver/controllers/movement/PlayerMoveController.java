package com.aionemu.gameserver.controllers.movement;

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

	private float fallStartZ = Float.NaN;
	private float lastZ = Float.NaN;
	private byte lastMovementMask;
	private long lastPositionFromClientMillis;
	private WorldPosition lastPositionFromClient;
	private long lastRandomMoveLocEffectTimeMillis;

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
	 * Forgets where the current fall started, so a teleport doesn't count as one.
	 */
	public void resetFalling() {
		fallStartZ = Float.NaN;
		lastZ = Float.NaN;
	}

	/**
	 * @return True if the player is below z 0 and more than 2 m under the ground. Where there is no ground, z 0 counts as ground.
	 */
	private boolean isBelowWorld(float z) {
		return z <= 0 && (z < -2 || !Float.isNaN(GeoService.getInstance().getZ(owner, 5000, z + 2)));
	}

	private void dieFromFall() {
		resetFalling();
		if (owner.isDead())
			return;
		if (owner.getController().die(TYPE.FALL_DAMAGE, LOG.REGULAR, owner))
			PlayerReviveService.bindRevive(owner);
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
