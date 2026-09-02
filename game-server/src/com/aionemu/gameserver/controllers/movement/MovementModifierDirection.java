package com.aionemu.gameserver.controllers.movement;

import com.aionemu.gameserver.controllers.movement.MovementModifierState.MoveState;

/**
 * Movement direction relative to the own heading, which determines the applied movement modifiers.
 * <p>
 * Left and right are not distinguished, both count as sideways.
 *
 * @see com.aionemu.gameserver.utils.stats.StatFunctions#adjustStatByMovementModifier
 */
public enum MovementModifierDirection {

	NONE(MoveState.NONE),
	FORWARD(MoveState.FORWARD),
	BACKWARD(MoveState.BACKWARD),
	SIDEWAYS(MoveState.SIDEWAYS);

	private final int moveStateFlag;

	MovementModifierDirection(int moveStateFlag) {
		this.moveStateFlag = moveStateFlag;
	}

	public int getMoveStateFlag() {
		return moveStateFlag;
	}
}
