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
	private final MovementModifierState movementModifierState = new MovementModifierState();

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
				onMovementStopped();
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

		// server side controlled movement (fear, confuse) is not affected by the activation and deactivation delays of movement modifiers, since those
		// only apply to movement requested by the client. The speed penalty for moving sideways or backwards applies immediately.
		float currentSpeed = StatFunctions.adjustSpeedByMovementModifier(calculateMovementDirection(), owner.getGameStats().getMovementSpeedFloat());
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
		onMovementStopped();
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
	}

	/**
	 * Feeds the currently requested movement direction into the movement modifier state. Must only be called for movements requested by the client, since
	 * movement modifiers don't apply to server side controlled movement (there are no direction arrows while under fear, for example).
	 * <p>
	 * The requested destination is used instead of the covered distance, because the client sends position updates only about every 672 ms. Movement
	 * during that period is not necessarily a straight line, so the covered distance would be misclassified whenever the heading changes (turning with
	 * the mouse while running forward would look like sideways movement).
	 */
	public void updateMovementModifierDirection() {
		movementModifierState.setMoveState(calculateMovementDirection().getMoveStateFlag());
	}

	/**
	 * @return The direction of the movement towards the current destination, or {@link MovementModifierDirection#NONE} if there is no destination to move
	 *         to (stop packet, turning on the spot or jumping on the spot). Must be called after the position update, since the direction is calculated
	 *         from the current position towards the destination.
	 */
	private MovementModifierDirection calculateMovementDirection() {
		if (PositionUtil.getDistance(owner.getX(), owner.getY(), targetDestX, targetDestY) < MOVE_CHECK_OFFSET)
			return MovementModifierDirection.NONE;
		return calculateDirection(PositionUtil.getHeadingTowards(owner.getX(), owner.getY(), targetDestX, targetDestY), heading);
	}

	/**
	 * Compares two client headings (120 units of 3° each), folds the difference into 0..60 and treats <= 15 units as forward and >= 45 units as
	 * backward, i.e. a 90° wide forward cone.
	 * <p>
	 * The arithmetic must stay integer. Comparing degrees instead is asymmetric, because only the own heading is quantized to 3° steps while the angle
	 * towards the destination is not: a diagonal then lands at 45° ± 1.5° and falls on either side of the boundary depending on which diagonal it is.
	 * Quantizing both sides puts every diagonal at exactly 15 units.
	 */
	static MovementModifierDirection calculateDirection(byte destinationHeading, byte ownHeading) {
		int headingDiff = (destinationHeading + 120 - ownHeading) % 120;
		if (headingDiff > 60)
			headingDiff = 120 - headingDiff;
		if (headingDiff <= 15)
			return MovementModifierDirection.FORWARD;
		return headingDiff >= 45 ? MovementModifierDirection.BACKWARD : MovementModifierDirection.SIDEWAYS;
	}

	public void onMovementStopped() {
		movementModifierState.setMoveState(MovementModifierState.MoveState.NONE);
	}

	/**
	 * @return The currently active movement directions as a {@link MovementModifierState.MoveState} bit mask.
	 */
	public int getMoveState() {
		return movementModifierState.getMoveState();
	}
}
