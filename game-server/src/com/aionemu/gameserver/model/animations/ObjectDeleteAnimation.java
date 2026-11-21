package com.aionemu.gameserver.model.animations;

/**
 * These IDs are for use with {@link com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE SM_DELETE} and
 * {@link com.aionemu.gameserver.network.aion.serverpackets.SM_PET SM_PET}.
 * 
 * @author Neon
 */
public enum ObjectDeleteAnimation {

	/*
	 * 4.7.5:
	 * 0 - 00000 - instant
	 * 1 - 00001 - fade out
	 * 2 - 00010 - fade out with beam
	 * 3 - 00011 - fade out with beam
	 * 4 - 00100 - fade out with beam
	 * 5 - 00101 - instant
	 * 6 - 00110 - instant
	 * 7 - 00111 - instant
	 * 8 - 01000 - instant
	 * 9 - 01001 - instant
	 * 10 - 01010 - instant
	 * 11 - 01011 - jump in silhouette (player only)
	 * 12 - 01100 - jump in silhouette (player only)
	 * 13 - 01101 - fade out with beam
	 * 14 - 01110 - instant
	 * 15 - 01111 - fade out
	 * 16 - 10000 - instant
	 * 17 - 10001 - instant
	 * 18 - 10010 - instant
	 * 19 - 10011 - disappear after a short delay
	 */

	NONE(0),
	FADE_OUT(1),
	FADE_OUT_BEAM(2),
	JUMP_IN(11), // players and humanoids(needs more tests but works with asmo/ely npcs) only
	DELAYED(19); // deletes also flags from map

	private final byte animationId;

	ObjectDeleteAnimation(int animationId) {
		this.animationId = (byte) animationId;
	}

	public byte getId() {
		return animationId;
	}
}
