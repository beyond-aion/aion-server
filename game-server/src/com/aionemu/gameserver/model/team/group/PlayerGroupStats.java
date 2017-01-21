package com.aionemu.gameserver.model.team.group;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.google.common.base.Predicate;

/**
 * @author ATracer
 */
public class PlayerGroupStats implements Predicate<Player> {

	private final PlayerGroup group;
	private int minExpPlayerLevel;
	private int maxExpPlayerLevel;

	Player minLevelPlayer;
	Player maxLevelPlayer;

	PlayerGroupStats(PlayerGroup group) {
		this.group = group;
	}

	public void onAddPlayer(PlayerGroupMember member) {
		group.applyOnMembers(this);
		calculateExpLevels();
	}

	public void onRemovePlayer(PlayerGroupMember member) {
		group.applyOnMembers(this);
	}

	private void calculateExpLevels() {
		minExpPlayerLevel = minLevelPlayer.getLevel();
		maxExpPlayerLevel = maxLevelPlayer.getLevel();
		minLevelPlayer = null;
		maxLevelPlayer = null;
	}

	@Override
	public boolean apply(Player player) {
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
		return true;
	}

	public int getMinExpPlayerLevel() {
		return minExpPlayerLevel;
	}

	public int getMaxExpPlayerLevel() {
		return maxExpPlayerLevel;
	}

}
