package com.aionemu.gameserver.model.instance.instancescore;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.autogroup.AGPlayer;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.playerreward.HarmonyGroupReward;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
public class HarmonyArenaScore extends PvPArenaScore {

	private final List<HarmonyGroupReward> groups = new ArrayList<>();

	public HarmonyArenaScore(WorldMapInstance instance) {
		super(instance);
	}

	public HarmonyGroupReward getHarmonyGroupReward(int objectId) {
		return groups.stream().filter(reward -> reward.containsPlayer(objectId)).findFirst().orElse(null);
	}

	public List<HarmonyGroupReward> getHarmonyGroupInside() {
		List<HarmonyGroupReward> harmonyGroups = new ArrayList<>();
		for (HarmonyGroupReward group : groups) {
			for (AGPlayer agp : group.getAssociatedPlayers()) {
				Player p = instance.getPlayer(agp.getObjectId());
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
	public int getRank(int points) {
		List<HarmonyGroupReward> sortedByPoints = groups.stream().sorted((r1, r2) -> Integer.compare(r2.getPoints(), r1.getPoints())).toList();
		int rank = -1;
		for (HarmonyGroupReward reward : sortedByPoints) {
			if (reward.getPoints() >= points) {
				rank++;
			}
		}
		return rank;
	}

	@Override
	public int getTotalPoints() {
		return groups.stream().mapToInt(InstancePlayerReward::getPoints).sum();
	}

	@Override
	public void clear() {
		groups.clear();
		super.clear();
	}

}
