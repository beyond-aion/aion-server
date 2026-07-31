package com.aionemu.gameserver.controllers.movement;

/**
 * Movement direction relative to the own heading, which determines the applied movement modifiers.<br>
 * The constants are declared in ascending priority order: while several directions are active at the same time, the modifiers of the last one apply
 * (verified on retail, see {@link MovementModifierState}).
 *
 * @see com.aionemu.gameserver.utils.stats.StatFunctions#adjustStatByMovementModifier
 */
public enum MovementModifierDirection {
	NONE,
	BACKWARD,
	SIDEWAYS,
	FORWARD
}
