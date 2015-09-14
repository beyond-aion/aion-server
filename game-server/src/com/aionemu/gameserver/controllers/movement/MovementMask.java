package com.aionemu.gameserver.controllers.movement;

/**
 * @author Mr. Poke
 */
public class MovementMask {

	public static final byte IMMEDIATE = (byte) 0x00;

	public static final byte GLIDE = (byte) 0xC4;
	public static final byte FALL = (byte) 0x08;
	public static final byte VEHICLE = (byte) 0x10;
	public static final byte MOUSE = (byte) 0x20;
	public static final byte STARTMOVE = (byte) 0xC0;

	public static final byte NPC_WALK_SLOW = (byte) 0xEA;
	public static final byte NPC_WALK_FAST = (byte) 0xE8;
	public static final byte NPC_RUN_SLOW = (byte) 0xE4;
	public static final byte NPC_RUN_FAST = (byte) 0xE2;
	public static final byte NPC_STARTMOVE = (byte) 0xE0;
}
