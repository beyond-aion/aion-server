package com.aionemu.gameserver.controllers.movement;

/**
 * @author Neon
 */
public class GlideFlag {

	public static final byte NONE = (byte) 0x00;
	public static final byte WEAK_UPWIND = (byte) 0x10;
	public static final byte MEDIUM_UPWIND = (byte) 0x20;
	public static final byte STRONG_UPWIND = WEAK_UPWIND + MEDIUM_UPWIND; // 0x30
	public static final byte GEYSER = (byte) 0x80;
}
