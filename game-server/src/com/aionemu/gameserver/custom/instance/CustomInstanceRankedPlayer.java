package com.aionemu.gameserver.custom.instance;

import com.aionemu.gameserver.model.PlayerClass;

/**
 * @author Neon
 */
public class CustomInstanceRankedPlayer extends CustomInstanceRank {

	private final String name;
	private final PlayerClass playerClass;

	public CustomInstanceRankedPlayer(int playerId, int rank, long lastEntry, int maxRank, int dps, String name, PlayerClass playerClass) {
		super(playerId, rank, lastEntry, maxRank, dps);
		this.name = name;
		this.playerClass = playerClass;
	}

	public String getName() {
		return name;
	}

	public PlayerClass getPlayerClass() {
		return playerClass;
	}
}
