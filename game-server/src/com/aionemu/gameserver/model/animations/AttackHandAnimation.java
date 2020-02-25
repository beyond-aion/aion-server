package com.aionemu.gameserver.model.animations;

public enum AttackHandAnimation {

	MAIN_HAND(0),
	// only some npcs support off hand & random animation e.g. Hyperion
	OFF_HAND(1),
	RANDOM(2);

	private final byte animationId;

	AttackHandAnimation(int animationId) {
		this.animationId = (byte) animationId;
	}

	public byte getId() {
		return animationId;
	}
}
