package com.aionemu.gameserver.model.autogroup;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancescore.HarmonyArenaScore;
import com.aionemu.gameserver.model.instance.playerreward.HarmonyGroupReward;
import com.aionemu.gameserver.model.team.TeamType;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz
 */
public class AutoHarmonyInstance extends AutoInstance {

	private List<AGPlayer> group1 = new ArrayList<>();
	private List<AGPlayer> group2 = new ArrayList<>();

	public AutoHarmonyInstance(AutoGroupType agt) {
		super(agt);
	}

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		HarmonyArenaScore reward = (HarmonyArenaScore) instance.getInstanceHandler().getInstanceScore();
		reward.addHarmonyGroup(new HarmonyGroupReward(0, 12000, (byte) 7, group1, agt));
		reward.addHarmonyGroup(new HarmonyGroupReward(1, 12000, (byte) 7, group2, agt));
	}

	@Override
	public AGQuestion addPlayer(Player player, SearchInstance searchInstance) {
		super.writeLock();
		try {
			if (!satisfyTime(searchInstance) || (players.size() >= getMaxPlayers())) {
				return AGQuestion.FAILED;
			}
			AGQuestion result;
			if (searchInstance.getEntryRequestType().isGroupEntry()) {
				result = canAddGroup(group1, player, searchInstance);
				if (result.isFailed()) {
					result = canAddGroup(group2, player, searchInstance);
				}
				return result;
			}
			result = canAddPlayer(group1, player);
			if (result.isFailed()) {
				result = canAddPlayer(group2, player);
			}
			return result;
		} finally {
			super.writeUnlock();
		}
	}

	@Override
	public void onPressEnter(Player player) {
		super.onPressEnter(player);
		if (agt.isHarmonyArena()) {
			if (!decrease(player, 186000184, 1)) {
				players.remove(player.getObjectId());
				PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(agt.getInstanceMaskId(), 5));
				if (players.isEmpty()) {
					AutoGroupService.getInstance().unregisterAndDestroyInstance(instance.getInstanceId());
				}
				return;
			}
		}
		((HarmonyArenaScore) instance.getInstanceHandler().getInstanceScore()).portToPosition(player);
		instance.register(player.getObjectId());
	}

	@Override
	public void onEnterInstance(Player player) {
		super.onEnterInstance(player);
		if (player.isInGroup()) {
			return;
		}
		int objectId = player.getObjectId();
		List<AGPlayer> group = getGroup(objectId);
		if (group != null) {
			List<Player> _players = getPlayerFromGroup(group);
			_players.remove(player);
			if (_players.size() == 1 && !_players.get(0).isInGroup()) {
				HarmonyArenaScore reward = (HarmonyArenaScore) instance.getInstanceHandler().getInstanceScore();
				HarmonyGroupReward r = reward.getHarmonyGroupReward(objectId);
				PlayerGroup newGroup = PlayerGroupService.createGroup(_players.get(0), player, TeamType.AUTO_GROUP, r.getId());
				int groupId = newGroup.getObjectId();
				if (!instance.isRegistered(groupId)) {
					instance.register(groupId);
				}
			} else if (!_players.isEmpty() && _players.get(0).isInGroup()) {
				PlayerGroupService.addPlayer(_players.get(0).getPlayerGroup(), player);
			}
			if (!instance.isRegistered(objectId)) {
				instance.register(objectId);
			}
		}
	}

	@Override
	public void onLeaveInstance(Player player) {
		unregister(player);
		PlayerGroupService.removePlayer(player);
	}

	@Override
	public void unregister(Player player) {
		AGPlayer agp = players.get(player.getObjectId());
		if (agp != null) {
			if (group1.contains(agp)) {
				group1.remove(agp);
			} else if (group2.contains(agp)) {
				group2.remove(agp);
			}
		}
		super.unregister(player);
	}

	@Override
	public void clear() {
		super.clear();
		group1.clear();
		group2.clear();
	}

	private List<Player> getPlayerFromGroup(List<AGPlayer> group) {
		List<Player> _players = new ArrayList<>();
		for (AGPlayer agp : group) {
			for (Player p : instance.getPlayersInside()) {
				if (p.getObjectId() == agp.getObjectId()) {
					_players.add(p);
					break;
				}
			}
		}
		return _players;
	}

	private List<AGPlayer> getGroup(Integer obj) {
		AGPlayer agp = players.get(obj);
		if (agp != null) {
			if (group1.contains(agp)) {
				return group1;
			} else if (group2.contains(agp)) {
				return group2;
			}
		}
		return null;
	}

	private AGQuestion canAddGroup(List<AGPlayer> group, Player player, SearchInstance searchInstance) {
		if (group.size() > 0) {
			if (!group.get(0).getRace().equals(player.getRace())) {
				return AGQuestion.FAILED;
			}
		}
		if (group.size() + searchInstance.getMembers().size() <= 3) {
			for (Player member : player.getPlayerGroup().getOnlineMembers()) {
				int obj = member.getObjectId();
				if (searchInstance.getMembers().contains(obj)) {
					AGPlayer agp = new AGPlayer(member);
					group.add(agp);
					players.put(obj, agp);
				}
			}
			return instance != null ? AGQuestion.ADDED : (players.size() == getMaxPlayers() ? AGQuestion.READY : AGQuestion.ADDED);
		}
		return AGQuestion.FAILED;
	}

	private AGQuestion canAddPlayer(List<AGPlayer> group, Player player) {
		int obj = player.getObjectId();
		AGPlayer agp = new AGPlayer(player);
		if (group.size() < 3) {
			if (group.isEmpty()) {
				group.add(agp);
				players.put(obj, agp);
				return AGQuestion.ADDED;
			} else if (getAGPlayerByIndex(group, 0).getRace().equals(player.getRace())) {
				group.add(agp);
				players.put(obj, agp);
				return instance != null ? AGQuestion.ADDED : (players.size() == getMaxPlayers() ? AGQuestion.READY : AGQuestion.ADDED);
			}
		}
		return AGQuestion.FAILED;
	}

	private AGPlayer getAGPlayerByIndex(List<AGPlayer> group, int index) {
		return group.get(index);
	}

}
