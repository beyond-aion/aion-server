package com.aionemu.gameserver.model.instance.instancereward;

import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.sort;
import static ch.lambdaj.Lambda.sum;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javolution.util.FastTable;

import com.aionemu.gameserver.model.autogroup.AGPlayer;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.playerreward.HarmonyGroupReward;
import com.aionemu.gameserver.model.instance.playerreward.InstancePlayerReward;
import com.aionemu.gameserver.network.aion.instanceinfo.HarmonyScoreInfo;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_SCORE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 *
 * @author xTz
 */
public class HarmonyArenaReward extends PvPArenaReward{

	private FastTable<HarmonyGroupReward> groups = new FastTable<>();
	public HarmonyArenaReward(Integer mapId, int instanceId, WorldMapInstance instance) {
		super(mapId, instanceId, instance);
	}

	public HarmonyGroupReward getHarmonyGroupReward(Integer object) {
		for (InstancePlayerReward reward : groups) {
			HarmonyGroupReward harmonyReward = (HarmonyGroupReward) reward;
			if (harmonyReward.containPlayer(object)) {
				return harmonyReward;
			}
		}
		return null;
	}

	public List<HarmonyGroupReward> getHarmonyGroupInside() {
		List<HarmonyGroupReward> harmonyGroups = new ArrayList<>();
		for (HarmonyGroupReward group : groups) {
			for (AGPlayer agp : group.getAGPlayers()) {
				if (agp.isInInstance()) {
					harmonyGroups.add(group);
					break;
				}
			}
		}
		return harmonyGroups;
	}

	public FastTable<Player> getPlayersInside(HarmonyGroupReward group) {
		FastTable<Player> players = new FastTable<>();
		for (Player playerInside : instance.getPlayersInside()) {
			if (group.containPlayer(playerInside.getObjectId())) {
				players.add(playerInside);
			}
		}
		return players;
	}

	public void addHarmonyGroup(HarmonyGroupReward reward) {
		groups.add(reward);
	}

	public FastTable<HarmonyGroupReward> getGroups() {
		return groups;
	}

	public void sendPacket(final int type, final Integer object) {
		instance.doOnAllPlayers((Player player) -> {
            PacketSendUtility.sendPacket(player, new SM_INSTANCE_SCORE(new HarmonyScoreInfo(HarmonyArenaReward.this, type, object == null ? player.getObjectId() : object), getInstanceReward(), getTime()));
        });
	}

	@Override
	public int getRank(int points) {
		int rank = -1;
		for (HarmonyGroupReward reward : sortGroupPoints()) {
			if (reward.getPoints() >= points) {
				rank++;
			}
		}
		return rank;
	}

	public List<HarmonyGroupReward> sortGroupPoints() {
		return sort(groups, on(HarmonyGroupReward.class).getPoints(), new Comparator<Integer>() {

			@Override
			public int compare(Integer o1, Integer o2) {
				return o2 != null ? o2.compareTo(o1) : -o1.compareTo(o2);
			}

		});
	}

	@Override
	public int getTotalPoints() {
		return sum(groups, on(HarmonyGroupReward.class).getPoints());
	}

	@Override
	public void clear() {
		groups.clear();
		super.clear();
	}

}
