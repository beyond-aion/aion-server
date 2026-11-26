package com.aionemu.gameserver.model.animations;

/**
 * Which of an attacker's auto attack animations plays, named after the value SM_ATTACK carries for it. An NPC's model counts its one to three
 * animations from 0, a player's client counts its two from 1, so a player plays {@link #ANIMATION_1} or {@link #ANIMATION_2}.
 */
public enum AttackAnimation {

	ANIMATION_0(0),
	ANIMATION_1(1),
	ANIMATION_2(2);

	private final byte animationId;

	AttackAnimation(int animationId) {
		this.animationId = (byte) animationId;
	}

	public byte getId() {
		return animationId;
	}

	public static AttackAnimation getById(int id) {
		for (AttackAnimation animation : values())
			if (animation.animationId == id)
				return animation;
		return null;
	}
}
