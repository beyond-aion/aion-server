package com.aionemu.gameserver.model.animations;

/**
 * These IDs are for use with {@link com.aionemu.gameserver.network.aion.serverpackets.SM_TELEPORT_LOC SM_TELEPORT_LOC}.
 * 
 * @author xTz, Neon
 */
public enum TeleportAnimation {

	/*
	 * 4.7.5:
	 * 0 - 00000 - fade out
	 * 1 - 00001 - fade out with beam
	 * 2 - 00010 - fade out
	 * 3 - 00011 - jump in silhouette
	 * 4 - 00100 - jump in silhouette (animates statues)
	 * 5 - 00101 - fade out with beam
	 * 6 - 00110 - fade out with beam
	 * 7 - 00111 - fade out
	 * 8 - 01000 - jump in
	 */

	NONE(0),
	FADE_OUT_BEAM(1),
	FADE_OUT(2),
	JUMP_IN(3),
	JUMP_IN_STATUE(4),
	JUMP_IN_GATE(8),
	/* for custom battlegrounds/pvp-maps only */
	BATTLEGROUND(0);

	private final byte animationId;

	TeleportAnimation(int animationId) {
		this.animationId = (byte) animationId;
	}

	public byte getId() {
		return animationId;
	}

	public ArrivalAnimation getDefaultArrivalAnimation() {
		switch (this) {
			case FADE_OUT_BEAM:
				return ArrivalAnimation.FADE_IN_BEAM;
			case JUMP_IN_STATUE:
				return ArrivalAnimation.JUMP_OUT_CAMERA_FRONT;
			case JUMP_IN:
			case JUMP_IN_GATE:
				return ArrivalAnimation.JUMP_OUT_CAMERA_BEHIND;
			case BATTLEGROUND:
				return ArrivalAnimation.LANDING_GLOW;
			default:
				return ArrivalAnimation.LANDING;
		}
	}

	public ObjectDeleteAnimation getDefaultObjectDeleteAnimation() {
		switch (this) {
			case FADE_OUT_BEAM:
				return ObjectDeleteAnimation.FADE_OUT_BEAM;
			case JUMP_IN:
			case JUMP_IN_GATE:
			case JUMP_IN_STATUE:
				return ObjectDeleteAnimation.JUMP_IN;
			default:
				return ObjectDeleteAnimation.FADE_OUT;
		}
	}
}
