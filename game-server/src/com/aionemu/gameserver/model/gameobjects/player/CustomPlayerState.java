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
	NO_WHISPERS_MODE(1 << 5),
	ENEMY_OF_ALL_NPCS(1 << 6),
	ENEMY_OF_ALL_PLAYERS(1 << 7),
	NEUTRAL_TO_ALL_NPCS(1 << 8),
	NEUTRAL_TO_ALL_PLAYERS(1 << 9),
	ENEMY_OF_EVERYONE(ENEMY_OF_ALL_NPCS.getMask() | ENEMY_OF_ALL_PLAYERS.getMask()),
	NEUTRAL_TO_EVERYONE(NEUTRAL_TO_ALL_NPCS.getMask() | NEUTRAL_TO_ALL_PLAYERS.getMask());

	int bitMask;

	CustomPlayerState(int bitMask) {
		this.bitMask = bitMask;
	}

	int getMask() {
		return bitMask;
	}
}
