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
import com.aionemu.gameserver.services.autogroup.AutoGroupUtility;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author xTz, Estrayl
 */
public class AutoHarmonyInstance extends AutoInstance {

	private final List<AGPlayer> group1 = new ArrayList<>();
	private final List<AGPlayer> group2 = new ArrayList<>();

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
	public AGQuestion addLookingForParty(LookingForParty lookingForParty) {
		super.writeLock();
		try {
			if (isRegistrationDisabled(lookingForParty) || registeredAGPlayers.size() >= getMaxPlayers())
				return AGQuestion.FAILED;

			AGQuestion question = canAddParty(group1, lookingForParty);
			if (question == AGQuestion.FAILED)
				question = canAddParty(group2, lookingForParty);
			return question;
		} finally {
			super.writeUnlock();
		}
	}

	@Override
	public void onPressEnter(Player player) {
		if (agt.isHarmonyArena()) {
			if (!removeItem(player, 186000184, 1)) {
				registeredAGPlayers.remove(player.getObjectId());
				PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(agt.getTemplate().getMaskId(), 5));
				if (registeredAGPlayers.isEmpty())
					AutoGroupService.getInstance().destroyIfPossible(this);
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
		AGPlayer agp = registeredAGPlayers.get(player.getObjectId());
		if (agp != null) {
			if (group1.contains(agp))
				group1.remove(agp);
			else
				group2.remove(agp);
		}
		super.unregister(player);
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
		AGPlayer agp = registeredAGPlayers.get(obj);
		if (agp != null) {
			if (group1.contains(agp))
				return group1;
			else if (group2.contains(agp))
				return group2;
		}
		return null;
	}

	private AGQuestion canAddParty(List<AGPlayer> group, LookingForParty lfp) {
		if (group.size() + lfp.getMemberObjectIds().size() > 3)
			return AGQuestion.FAILED;
		if (!group.isEmpty() && group.get(0).getRace() != lfp.getRace())
			return AGQuestion.FAILED;

		for (int objectId : lfp.getMemberObjectIds()) {
			AGPlayer agp = AutoGroupUtility.getNewAutoGroupPlayer(objectId);
			if (agp != null) {
				group.add(agp);
				registeredAGPlayers.put(objectId, agp);
			}
		}
		return instance != null ? AGQuestion.ADDED : registeredAGPlayers.size() == getMaxPlayers() ? AGQuestion.READY : AGQuestion.ADDED;
	}
}
