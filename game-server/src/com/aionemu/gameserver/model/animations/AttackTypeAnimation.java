package com.aionemu.gameserver.model.animations;

public enum AttackTypeAnimation {

	MELEE(0),
	RANGED(1);

	private final byte animationId;

	AttackTypeAnimation(int animationId) {
		this.animationId = (byte) animationId;
	}

	public byte getId() {
		return animationId;
	}
}
