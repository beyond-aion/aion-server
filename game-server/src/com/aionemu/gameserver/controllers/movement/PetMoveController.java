package com.aionemu.gameserver.controllers.movement;

import com.aionemu.gameserver.model.gameobjects.Pet;

/**
 * @author ATracer
 */
public class PetMoveController extends CreatureMoveController<Pet> {

	protected byte movementMask;

	public PetMoveController() {
		super(null);// not used yet
	}

	@Override
	public void moveToDestination() {
	}

	@Override
	public float getTargetX2() {
		return targetDestX;
	}

	@Override
	public float getTargetY2() {
		return targetDestY;
	}

	@Override
	public float getTargetZ2() {
		return targetDestZ;
	}

	@Override
	public void setNewDirection(float x2, float y2, float z2) {
		setNewDirection(x2, y2, z2, (byte) 0);
	}

	@Override
	public void setNewDirection(float x, float y, float z, byte heading) {
		this.targetDestX = x;
		this.targetDestY = y;
		this.targetDestZ = z;
		this.heading = heading;
	}

	@Override
	public void startMovingToDestination() {
	}

	@Override
	public void abortMove() {
	}

	@Override
	public byte getMovementMask() {
		return movementMask;
	}

	@Override
	public boolean isInMove() {
		return true;
	}

	@Override
	public void setInMove(boolean value) {
	}
}
