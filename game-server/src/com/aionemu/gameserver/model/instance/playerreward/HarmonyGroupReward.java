package com.aionemu.gameserver.model.instance.playerreward;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.autogroup.AGPlayer;
import com.aionemu.gameserver.model.templates.rewards.ArenaRewardItem;

/**
 * @author xTz
 */
public class HarmonyGroupReward extends PvPArenaPlayerReward {

	private final List<AGPlayer> players = new ArrayList<>();
	private final int grpObjectId;

	public HarmonyGroupReward(int objectId, int timeBonus, byte buffId, int grpObjectId) {
		super(objectId, timeBonus, buffId);
		this.grpObjectId = grpObjectId;
		setCourageInsignia(new ArenaRewardItem(186000137, 0, 0, 0));
	}

	public List<AGPlayer> getAssociatedPlayers() {
		return players;
	}

	public boolean containsPlayer(int objectId) {
		return players.stream().anyMatch(agp -> agp.objectId() == objectId);
	}

	public AGPlayer getAGPlayer(int objectId) {
		for (AGPlayer agp : players) {
			if (agp.objectId() == objectId) {
				return agp;
			}
		}
		return null;
	}

	public void addPlayer(AGPlayer player) {
		players.add(player);
	}

	public int getGrpObjectId() {
		return grpObjectId;
	}

}
