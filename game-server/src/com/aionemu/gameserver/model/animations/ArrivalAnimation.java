package com.aionemu.gameserver.model.animations;

/**
 * These IDs are for use with {@link com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO SM_PLAYER_INFO}.
 * 
 * @author Neon
 */
public enum ArrivalAnimation {

	/*
	 * 4.7.5:
	 *  0 - 00000 - instant
	 *  1 - 00001 - instant
	 *  2 - 00010 - landing through circles
	 *  3 - 00011 - landing through circles
	 *  4 - 00100 - fade in with beam
	 *  5 - 00101 - instant
	 *  6 - 00110 - instant
	 *  7 - 00111 - landing through circles
	 *  8 - 01000 - instant
	 *  9 - 01001 - instant
	 * 10 - 01010 - jump out
	 * 11 - 01011 - jump out, camera in front
	 * 12 - 01100 - landing through circles
	 * 13 - 01101 - instant
	 * 14 - 01110 - instant
	 * 15 - 01111 - instant
	 * 16 - 10000 - instant
	 * 17 - 10001 - instant
	 * 18 - 10010 - glowing landing
	 */

	NONE(0),
	LANDING(2),
	FADE_IN_BEAM(4),
	JUMP_OUT_CAMERA_BEHIND(10),
	JUMP_OUT_CAMERA_FRONT(11),
	LANDING_GLOW(18);

	private final byte animationId;

	ArrivalAnimation(int animationId) {
		this.animationId = (byte) animationId;
	}

	public byte getId() {
		return animationId;
	}
}
