package com.aionemu.gameserver.controllers.movement;

import com.aionemu.gameserver.configs.main.FallDamageConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.stats.StatFunctions;
import com.aionemu.gameserver.world.WorldPosition;

/**
 * @author ATracer
 */
public class PlayerMoveController extends PlayableMoveController<Player> {

	private float fallDistance;
	private float lastFallZ;
	private byte lastMovementMask;
	private long lastPositionFromClientMillis;
	private WorldPosition lastPositionFromClient;
	private long lastRandomMoveLocEffectTimeMillis;

	public PlayerMoveController(Player owner) {
		super(owner);
	}

	@Override
	public void abortMove() {
		super.abortMove();
		stopFalling(owner.getZ());
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

	public void updateFalling(float newZ) {
		if (lastFallZ != 0) {
			fallDistance += lastFallZ - newZ;
			if (fallDistance >= FallDamageConfig.MAXIMUM_DISTANCE_MIDAIR && owner.getController().die(TYPE.FALL_DAMAGE, LOG.REGULAR, owner)) {
				PlayerReviveService.scheduleReviveAtBase(owner, 1000, 0);
				return;
			}
		}
		lastFallZ = newZ;
		owner.getObserveController().notifyMoveObservers();
	}

	public void stopFalling(float newZ) {
		if (lastFallZ == 0)
			return;

		if (!owner.isFlying() && !owner.isDead()) {
			fallDistance += lastFallZ - newZ;
			int damage = StatFunctions.calculateFallDamage(owner, fallDistance);
			if (damage > 0) {
				owner.getLifeStats().reduceHp(TYPE.FALL_DAMAGE, damage, 0, LOG.REGULAR, owner);
				owner.getObserveController().notifyAttackedObservers(owner, 0);
			}
		}
		fallDistance = 0;
		lastFallZ = 0;
		owner.getObserveController().notifyMoveObservers();
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
