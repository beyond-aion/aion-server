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

	public static final byte NPC_WALK_SLOW = (byte) 0xEA;
	public static final byte NPC_WALK_FAST = (byte) 0xE8;
	public static final byte NPC_RUN_SLOW = (byte) 0xE4;
	public static final byte NPC_RUN_FAST = (byte) 0xE2;
	public static final byte NPC_STARTMOVE = (byte) 0xE0;
}
