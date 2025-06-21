package com.aionemu.gameserver.model.instance.instancescore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.aionemu.gameserver.model.autogroup.AGPlayer;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.playerreward.HarmonyGroupReward;
import com.aionemu.gameserver.model.instance.playerreward.PvPArenaPlayerReward;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
public class HarmonyArenaScore extends PvPArenaScore {

	private final List<HarmonyGroupReward> groups = new ArrayList<>();

	public HarmonyArenaScore(WorldMapInstance instance) {
		super(instance);
	}

	public HarmonyGroupReward getGroupReward(int playerId) {
		return groups.stream().filter(reward -> reward.containsPlayer(playerId)).findFirst().orElse(null);
	}

	public List<HarmonyGroupReward> getHarmonyGroupInside() {
		List<HarmonyGroupReward> harmonyGroups = new ArrayList<>();
		for (HarmonyGroupReward group : groups) {
			for (AGPlayer agp : group.getAssociatedPlayers()) {
				Player p = instance.getPlayer(agp.objectId());
				if (p != null) {
					harmonyGroups.add(group);
					break;
				}
			}
		}
		return harmonyGroups;
	}

	public List<Player> getPlayersInside(HarmonyGroupReward group) {
		List<Player> players = new ArrayList<>();
		for (Player playerInside : instance.getPlayersInside()) {
			if (group.containsPlayer(playerInside.getObjectId())) {
				players.add(playerInside);
			}
		}
		return players;
	}

	public void addHarmonyGroup(HarmonyGroupReward reward) {
		groups.add(reward);
	}

	public List<HarmonyGroupReward> getGroups() {
		return groups;
	}

	@Override
	public int getRank(PvPArenaPlayerReward reward) {
		List<HarmonyGroupReward> sortedByPoints = groups.stream().sorted(Comparator.comparing(HarmonyGroupReward::getScorePoints)).toList();

		int rank = -1;
		for (PvPArenaPlayerReward r : sortedByPoints) {
			if (r.getScorePoints() >= reward.getScorePoints())
				rank++;
		}
		return rank;
	}

	@Override
	public int getTotalPoints() {
		return groups.stream().mapToInt(HarmonyGroupReward::getScorePoints).sum();
	}

	@Override
	public void clear() {
		groups.clear();
		super.clear();
	}

}
