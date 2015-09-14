package com.aionemu.gameserver.model;

/**
 * @author xTz
 */
public enum TeleportAnimation {

	NO_ANIMATION(0, 3),
	BEAM_ANIMATION(1, 3),
	JUMP_AIMATION(3, 10),
	JUMP_AIMATION_2(4, 10),
	JUMP_AIMATION_3(8, 3);

	private int startAnimation;
	private int endAnimation;

	TeleportAnimation(int startAnimation, int endAnimation) {
		this.startAnimation = startAnimation;
		this.endAnimation = endAnimation;
	}

	public int getStartAnimationId() {
		return startAnimation;
	}

	public int getEndAnimationId() {
		return endAnimation;
	}

	public boolean isNoAnimation() {
		return this.getStartAnimationId() == 0;
	}

}
