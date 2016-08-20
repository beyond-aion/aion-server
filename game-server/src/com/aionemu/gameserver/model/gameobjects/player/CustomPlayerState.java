package com.aionemu.gameserver.model.gameobjects.player;

/**
 * @author Neon
 */
public enum CustomPlayerState {
	WATCHING_CUTSCENE(1),
	INVULNERABLE(1 << 1),
	EVENT_MODE(1 << 2),
	TELEPORTATION_MODE(1 << 3),
	NO_SKILL_COOLDOWN_MODE(1 << 4),
	NO_WHISPERS_MODE(1 << 5);

	int bitMask;

	CustomPlayerState(int bitMask) {
		this.bitMask = bitMask;
	}

	int getMask() {
		return bitMask;
	}
}
