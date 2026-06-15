package com.aionemu.gameserver.controllers.movement;

import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOVE;
import com.aionemu.gameserver.taskmanager.tasks.MoveTaskManager;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;

/**
 * @author xTz
 */
public class SiegeWeaponMoveController extends SummonMoveController {

	private float pointX;
	private float pointY;
	private float pointZ;

	public SiegeWeaponMoveController(Summon owner) {
		super(owner);
	}

	/**
	 * @return if destination reached
	 */
	@Override
	public void moveToDestination() {
		if (!owner.canPerformMove() || (owner.getAi().getSubState() == AISubState.CAST)) {
			if (started.compareAndSet(true, false)) {
				setAndSendStopMove(owner);
				updateLastMove();
			}
			return;
		} else if (started.compareAndSet(false, true)) {
			updateLastMove();
			setAndSendStartMove(owner);
		}

		VisibleObject target = owner.getTarget();
		if (target != null) { // update target position, in case target moved
			pointX = target.getX();
			pointY = target.getY();
			pointZ = target.getZ();
		}
		moveToLocation(pointX, pointY, pointZ);
		updateLastMove();
	}

	@Override
	public void moveToTargetObject() {
		updateLastMove();
		MoveTaskManager.getInstance().addCreature(owner);
	}

	@Override
	public void abortMove() {
		super.abortMove();
		MoveTaskManager.getInstance().removeCreature(owner);
	}

	protected void moveToLocation(float targetX, float targetY, float targetZ) {
		boolean destinationChanged = targetX != targetDestX || targetY != targetDestY || targetZ != targetDestZ;
		float ownerX = owner.getX();
		float ownerY = owner.getY();
		float ownerZ = owner.getZ();

		if (targetX != targetDestX || targetY != targetDestY) {
			heading = PositionUtil.getHeadingTowards(ownerX, ownerY, targetX, targetY);
		}

		targetDestX = targetX;
		targetDestY = targetY;
		targetDestZ = targetZ;

		float currentSpeed = owner.getGameStats().getMovementSpeedFloat();
		float futureDistPassed = currentSpeed * (System.currentTimeMillis() - lastMoveUpdate) / 1000f;

		float dist = (float) PositionUtil.getDistance(ownerX, ownerY, ownerZ, targetX, targetY, targetZ);

		if (dist == 0) {
			return;
		}

		if (futureDistPassed > dist) {
			futureDistPassed = dist;
		}

		float distFraction = futureDistPassed / dist;
		float newX = (targetDestX - ownerX) * distFraction + ownerX;
		float newY = (targetDestY - ownerY) * distFraction + ownerY;
		float newZ = (targetDestZ - ownerZ) * distFraction + ownerZ;
		World.getInstance().updatePosition(owner, newX, newY, newZ, heading, true);
		if (destinationChanged) {
			movementMask = MovementMask.NPC_STARTMOVE;
			PacketSendUtility.broadcastPacket(owner, new SM_MOVE(owner));
		}
	}

}
