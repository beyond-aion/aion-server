package com.aionemu.gameserver.model.team.group;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
public class PlayerGroupStats {

	private final PlayerGroup group;
	private int minExpPlayerLevel;
	private int maxExpPlayerLevel;

	Player minLevelPlayer;
	Player maxLevelPlayer;

	PlayerGroupStats(PlayerGroup group) {
		this.group = group;
	}

	public void onAddPlayer(PlayerGroupMember member) {
		updateMinMaxLevelPlayers();
		calculateExpLevels();
	}

	public void onRemovePlayer(PlayerGroupMember member) {
		updateMinMaxLevelPlayers();
	}

	private void calculateExpLevels() {
		minExpPlayerLevel = minLevelPlayer.getLevel();
		maxExpPlayerLevel = maxLevelPlayer.getLevel();
		minLevelPlayer = null;
		maxLevelPlayer = null;
	}

	private void updateMinMaxLevelPlayers() {
		group.forEach(player -> {
			if (minLevelPlayer == null || maxLevelPlayer == null) {
				minLevelPlayer = player;
				maxLevelPlayer = player;
			} else {
				if (player.getCommonData().getExp() < minLevelPlayer.getCommonData().getExp()) {
					minLevelPlayer = player;
				}
				if (!player.isMentor() && player.getCommonData().getExp() > maxLevelPlayer.getCommonData().getExp()) {
					maxLevelPlayer = player;
				}
			}
		});
	}

	public int getMinExpPlayerLevel() {
		return minExpPlayerLevel;
	}

	public int getMaxExpPlayerLevel() {
		return maxExpPlayerLevel;
	}

}
