package com.aionemu.gameserver.controllers.movement;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOVE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public abstract class CreatureMoveController<T extends VisibleObject> {

	public static final float MOVE_CHECK_OFFSET = 0.1f;
	protected T owner;
	protected byte heading;
	protected long lastMoveUpdate = System.currentTimeMillis();
	protected boolean isInMove = false;
	protected transient AtomicBoolean started = new AtomicBoolean(false);

	public byte movementMask;
	protected float targetDestX;
	protected float targetDestY;
	protected float targetDestZ;
	private boolean isJumping = false;

	public CreatureMoveController(T owner) {
		this.owner = owner;
	}

	public void moveToDestination() {
	}

	public float getTargetX2() {
		return targetDestX;
	}

	public float getTargetY2() {
		return targetDestY;
	}

	public float getTargetZ2() {
		return targetDestZ;
	}

	public void setNewDirection(float x, float y, float z, byte heading) {
		this.heading = heading;
		setNewDirection(x, y, z);
	}

	protected void setNewDirection(float x, float y, float z) {
		this.targetDestX = x;
		this.targetDestY = y;
		this.targetDestZ = z;
	}

	public void startMovingToDestination() {
	}

	public void abortMove() {
	}

	protected void setAndSendStartMove(Creature owner) {
		setInMove(true);
		movementMask = MovementMask.NPC_STARTMOVE;
		PacketSendUtility.broadcastToSightedPlayers(owner, new SM_MOVE(owner));
	}

	protected void setAndSendStopMove(Creature owner) {
		setInMove(false);
		movementMask = MovementMask.IMMEDIATE;
		PacketSendUtility.broadcastToSightedPlayers(owner, new SM_MOVE(owner));
	}

	public final void updateLastMove() {
		lastMoveUpdate = System.currentTimeMillis();
	}

	public long getLastMoveUpdate() {
		return lastMoveUpdate;
	}

	public byte getMovementMask() {
		return movementMask;
	}

	public boolean isJumping() {
		return isJumping;
	}

	public void setIsJumping(boolean isJumping) {
		this.isJumping = isJumping;
	}

	public boolean isInMove() {
		return isInMove;
	}

	public void setInMove(boolean value) {
		isInMove = value;
	}
}
