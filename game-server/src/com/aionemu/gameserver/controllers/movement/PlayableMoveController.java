package com.aionemu.gameserver.controllers.movement;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOVE;
import com.aionemu.gameserver.taskmanager.tasks.PlayerMoveTaskManager;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.stats.StatFunctions;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer base class for summon & player move controller
 */
public abstract class PlayableMoveController<T extends Creature> extends CreatureMoveController<T> {

	private boolean sendMovePacket = true;
	private MovementModifierDirection movementModifierDirection = MovementModifierDirection.NONE;

	public float vehicleX;
	public float vehicleY;
	public float vehicleZ;

	public float vectorX;
	public float vectorY;
	public float vectorZ;
	public byte glideFlag;
	public int unk1;
	public int unk2;
	public int geyserLocationId; // locationId from windstreams.xml

	public PlayableMoveController(T owner) {
		super(owner);
	}

	@Override
	public void startMovingToDestination() {
		updateLastMove();
		if (owner.canPerformMove()) {
			if (isControlled() && started.compareAndSet(false, true)) {
				this.movementMask = MovementMask.NPC_STARTMOVE;
				sendForcedMovePacket();
				PlayerMoveTaskManager.getInstance().addPlayer(owner);
			}
		}
	}

	private boolean isControlled() {
		return owner.getEffectController().isUnderFear() || owner.getEffectController().isConfused();
	}

	private void sendForcedMovePacket() {
		PacketSendUtility.broadcastPacketAndReceive(owner, new SM_MOVE(owner));
		sendMovePacket = false;
	}

	@Override
	public void moveToDestination() {
		if (!owner.canPerformMove()) {
			if (started.compareAndSet(true, false)) {
				setAndSendStopMove(owner);
				updateLastMove();
			}
			return;
		}

		if (sendMovePacket && isControlled()) {
			sendForcedMovePacket();
		}

		float x = owner.getX();
		float y = owner.getY();
		float z = owner.getZ();

		float dist = (float) PositionUtil.getDistance(x, y, z, targetDestX, targetDestY, targetDestZ);
		if (dist < 0.01f)
			return;

		float currentSpeed = StatFunctions.adjustStatByMovementModifier(owner, StatEnum.SPEED, owner.getGameStats().getMovementSpeedFloat());
		long msElapsed = System.currentTimeMillis() - lastMoveUpdate;
		float futureXYDistPassed = Math.min(currentSpeed * msElapsed / 1000f, dist);
		float futureZDistPassed = isJumping() ? Math.min(2 * msElapsed / 1000f, dist) : futureXYDistPassed;

		float distXYFraction = futureXYDistPassed / dist;
		float distZFraction = isJumping() ? futureZDistPassed / dist : distXYFraction;
		float newX = (targetDestX - x) * distXYFraction + x;
		float newY = (targetDestY - y) * distXYFraction + y;
		float newZ = (targetDestZ - z) * distZFraction + z;

		/*
		 * if ((movementMask & MovementMask.MOUSE) == 0) { targetDestX = newX + vectorX; targetDestY = newY + vectorY; targetDestZ = newZ + vectorZ; }
		 */

		World.getInstance().updatePosition(owner, newX, newY, newZ, heading, false);
		updateLastMove();
	}

	@Override
	public void abortMove() {
		started.set(false);
		PlayerMoveTaskManager.getInstance().removePlayer(owner);
		targetDestX = 0;
		targetDestY = 0;
		targetDestZ = 0;
		setAndSendStopMove(owner);
	}

	@Override
	public void setNewDirection(float x, float y, float z) {
		if (targetDestX != x || targetDestY != y || targetDestZ != z) {
			sendMovePacket = true;
		}
		super.setNewDirection(x, y, z);

		float relativeMovementAngle = PositionUtil.calculateAngleTowards(owner.getX(), owner.getY(), heading, targetDestX, targetDestY);
		if (relativeMovementAngle >= -67.5 && relativeMovementAngle <= 67.5)
			movementModifierDirection = MovementModifierDirection.FORWARD;
		else if (relativeMovementAngle <= -112.5 || relativeMovementAngle >= 112.5)
			movementModifierDirection = MovementModifierDirection.BACKWARD;
		else
			movementModifierDirection = MovementModifierDirection.SIDEWAYS;
	}

	public MovementModifierDirection getMovementDirection() {
		if (!isInMove() && System.currentTimeMillis() - lastMoveUpdate > 1000)
			return MovementModifierDirection.NONE;
		return movementModifierDirection;
	}

	public enum MovementModifierDirection {
		NONE,
		FORWARD,
		SIDEWAYS,
		BACKWARD
	}
}
