package com.aionemu.gameserver.model.autogroup;

import java.util.*;

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
 * @author xTz, Estrayl
 */
public class AutoHarmonyInstance extends AutoInstance {

	private final HashMap<Integer, List<AGPlayer>> groups = new HashMap<>();

	public AutoHarmonyInstance(AutoGroupType agt) {
		super(agt);
		groups.put(0, new ArrayList<>());
		groups.put(1, new ArrayList<>());
	}

	@Override
	public void onInstanceCreate(WorldMapInstance instance) {
		super.onInstanceCreate(instance);
		HarmonyArenaScore score = (HarmonyArenaScore) instance.getInstanceHandler().getInstanceScore();
		score.setDifficultyId(agt.getDifficultId());
	}

	@Override
	public AGQuestion addLookingForParty(LookingForParty lookingForParty) {
		super.writeLock();
		try {
			if (isRegistrationDisabled(lookingForParty) || registeredAGPlayers.size() >= getMaxPlayers())
				return AGQuestion.FAILED;

			AGQuestion question = canAddParty(groups.get(0), lookingForParty);
			if (question == AGQuestion.FAILED)
				question = canAddParty(groups.get(1), lookingForParty);
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
		int playerId = player.getObjectId();
		Map.Entry<Integer, List<AGPlayer>> groupEntry = getGroupEntry(playerId);
		if (groupEntry == null)
			return;

		HarmonyArenaScore score = (HarmonyArenaScore) instance.getInstanceHandler().getInstanceScore();
		List<Player> players = findPlayersInInstance(groupEntry.getValue());
		players.remove(player);

		if (players.isEmpty()) { // Create Group
			PlayerGroup newGroup = PlayerGroupService.createGroup(player, player, TeamType.AUTO_GROUP, 0);
			int groupId = newGroup.getObjectId();
			if (!instance.isRegistered(groupId)) {
				instance.register(groupId);
				HarmonyGroupReward reward = new HarmonyGroupReward(groupEntry.getKey(), 12000, (byte) 7, groupId);
				reward.addPlayer(registeredAGPlayers.get(player.getObjectId()));
				score.addHarmonyGroup(reward);
			}
		} else { // Add To Group
			PlayerGroup pg = players.getFirst().getPlayerGroup();
			PlayerGroupService.addPlayer(pg, player);
			HarmonyGroupReward reward = score.getGroupReward(pg.getLeader().getObjectId());
			reward.addPlayer(registeredAGPlayers.get(player.getObjectId()));
		}

		if (!instance.isRegistered(playerId)) {
			instance.register(playerId);
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
			groups.get(0).remove(agp);
			groups.get(1).remove(agp);
		}
		super.unregister(player);
	}

	private List<Player> findPlayersInInstance(List<AGPlayer> group) {
		List<Player> _players = new ArrayList<>();
		for (AGPlayer agp : group) {
			for (Player p : instance.getPlayersInside()) {
				if (p.getObjectId() == agp.objectId()) {
					_players.add(p);
					break;
				}
			}
		}
		return _players;
	}

	private Map.Entry<Integer, List<AGPlayer>> getGroupEntry(int playerObjId) {
		AGPlayer agp = registeredAGPlayers.get(playerObjId);
		if (agp != null) {
			Set<Map.Entry<Integer, List<AGPlayer>>> entrySet = groups.entrySet();
			for (Map.Entry<Integer, List<AGPlayer>> entry : entrySet) {
				if (entry.getValue().contains(agp))
					return entry;
			}
		}
		return null;
	}

	private AGQuestion canAddParty(List<AGPlayer> group, LookingForParty lfp) {
		if (group.size() + lfp.getMembers().size() > 3)
			return AGQuestion.FAILED;
		if (!group.isEmpty() && group.getFirst().race() != lfp.getRace())
			return AGQuestion.FAILED;

		for (Map.Entry<Integer, AGPlayer> entry : lfp.getMembers().entrySet()) {
			group.add(entry.getValue());
			registeredAGPlayers.put(entry.getKey(), entry.getValue());
		}
		return instance != null ? AGQuestion.ADDED : registeredAGPlayers.size() == getMaxPlayers() ? AGQuestion.READY : AGQuestion.ADDED;
	}
}
