package com.aionemu.gameserver.controllers.movement;

/**
 * @author Mr. Poke, Neon
 */
public class MovementMask {

	/**
	 * When stopping or instant action (this is zero, so a movement with any other flag has no immediate in it)
	 */
	public static final byte IMMEDIATE = (byte) 0x00;

	/**
	 * When the creature is thrown in an arc, e.g. by a jump trigger. Clients replay a move with this flag as a ballistic curve.
	 */
	public static final byte ARC = (byte) 0x02; // 2

	/**
	 * When gliding
	 */
	public static final byte GLIDE = (byte) 0x04; // 4

	/**
	 * When falling
	 */
	public static final byte FALL = (byte) 0x08; // 8

	/**
	 * When standing on moving objects like elevators
	 */
	public static final byte VEHICLE = (byte) 0x10; // 16

	/**
	 * Absolute destination coordinates (e.g. mouse related movement). Movements without this flag use relative directions.
	 */
	public static final byte ABSOLUTE = (byte) 0x20; // 32

	/**
	 * When changing direction or move state
	 */
	public static final byte MANUAL = (byte) 0x40; // 64

	/**
	 * When coords change, but not if heading updates
	 */
	public static final byte POSITION = (byte) 0x80; // 128

	/**
	 * Movement type reported while following a flight path. The client keeps sending normal move packets along the path, but its
	 * state packet names the path flight as a position update with the arc flag instead.
	 */
	public static final byte PATH_FLY = (byte) (POSITION | ARC); // 130

	public static final byte NPC_WALK_SLOW = (byte) 0xEA;
	public static final byte NPC_WALK_FAST = (byte) 0xE8;
	public static final byte NPC_RUN_SLOW = (byte) 0xE4;
	public static final byte NPC_RUN_FAST = (byte) 0xE2;
	public static final byte NPC_STARTMOVE = (byte) 0xE0;
}
