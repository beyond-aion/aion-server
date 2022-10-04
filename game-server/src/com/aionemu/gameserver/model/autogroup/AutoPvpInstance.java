package com.aionemu.gameserver.model.autogroup;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamType;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.services.autogroup.AutoGroupUtility;

/**
 * Includes Dredgion, Engulfed Ophidan Bridge, Idgel Dome, Iron Wall Warfront and Kamar Battlefield
 * 
 * @author Estrayl
 */
public class AutoPvpInstance extends AutoInstance {

	public AutoPvpInstance(AutoGroupType agt) {
		super(agt);
	}

	@Override
	public AGQuestion addLookingForParty(LookingForParty lookingForParty) {
		writeLock();
		try {
			if (isRegistrationDisabled(lookingForParty) || registeredAGPlayers.size() >= getMaxPlayers())
				return AGQuestion.FAILED;

			EntryRequestType ert = lookingForParty.getEntryRequestType();
			List<AGPlayer> playersByRace = getAGPlayersByRace(lookingForParty.getRace());
			if (ert == EntryRequestType.GROUP_ENTRY) {
				if (lookingForParty.getMemberObjectIds().size() + playersByRace.size() >= getMaxPlayers() / 2)
					return AGQuestion.FAILED;

				for (int objectId : lookingForParty.getMemberObjectIds()) {
					AGPlayer agp = AutoGroupUtility.getNewAutoGroupPlayer(objectId);
					if (agp != null)
						registeredAGPlayers.put(objectId, new AGPlayer(objectId));
				}
			} else {
				if (playersByRace.size() >= getMaxPlayers() / 2)
					return AGQuestion.FAILED;

				AGPlayer agp = AutoGroupUtility.getNewAutoGroupPlayer(lookingForParty.getLeaderObjId());
				if (agp == null)
					return AGQuestion.FAILED;
				registeredAGPlayers.put(lookingForParty.getLeaderObjId(), agp);
			}
			return instance == null && registeredAGPlayers.size() == getMaxPlayers() ? AGQuestion.READY : AGQuestion.ADDED;
		} finally {
			writeUnlock();
		}
	}

	@Override
	public void onEnterInstance(Player player) {
		super.onEnterInstance(player);
		List<Player> playersByRace = getPlayersByRace(player.getRace());
		playersByRace.remove(player);
		if (playersByRace.isEmpty()) {
			TemporaryPlayerTeam<?> team;
			if (getMaxPlayers() <= 12)
				team = PlayerGroupService.createGroup(player, player, TeamType.AUTO_GROUP, 0);
			else
				team = PlayerAllianceService.createAlliance(player, player, TeamType.AUTO_ALLIANCE);
			int teamId = team.getObjectId();
			if (!instance.isRegistered(teamId))
				instance.register(teamId);
		} else {
			if (playersByRace.get(0).isInGroup())
				PlayerGroupService.addPlayer(playersByRace.get(0).getPlayerGroup(), player);
			else
				PlayerAllianceService.addPlayer(playersByRace.get(0).getPlayerAlliance(), player);
		}
		int objectId = player.getObjectId();
		if (!instance.isRegistered(objectId))
			instance.register(objectId);
	}

	@Override
	public void onPressEnter(Player player) {
		super.onPressEnter(player);
		instance.getInstanceHandler().portToStartPosition(player);
	}

	@Override
	public void onLeaveInstance(Player player) {
		super.unregister(player);
		if (player.isInGroup())
			PlayerGroupService.removePlayer(player);
		else if (player.isInAlliance())
			PlayerAllianceService.removePlayer(player);
	}

	@Override
	public int getMaxPlayers() {
		return super.getMaxPlayers() * 2; // INSTANCE_COOLTIME only asks for faction specific count, so we need to double the amount here
	}
}
