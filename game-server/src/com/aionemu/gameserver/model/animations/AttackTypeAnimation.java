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

	/**
	 * @return The animation with the given id, or null if there is none
	 */
	public static AttackTypeAnimation getById(int id) {
		for (AttackTypeAnimation animation : values())
			if (animation.animationId == id)
				return animation;
		return null;
	}
}
