package com.aionemu.gameserver.model.animations;

/**
 * Which of an attacker's auto attack animations plays, counted from 0 as the client numbers them. An NPC's model has one to three of them.
 */
public enum AttackAnimation {

	FIRST(0),
	SECOND(1),
	THIRD(2);

	private final byte animationId;

	AttackAnimation(int animationId) {
		this.animationId = (byte) animationId;
	}

	public byte getId() {
		return animationId;
	}

	/**
	 * @return The animation with the given id, or null if there is none
	 */
	public static AttackAnimation getById(int id) {
		for (AttackAnimation animation : values())
			if (animation.animationId == id)
				return animation;
		return null;
	}
}
