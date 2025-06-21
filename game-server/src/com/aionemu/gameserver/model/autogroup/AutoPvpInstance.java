package com.aionemu.gameserver.model.autogroup;

import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamType;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;

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

			List<AGPlayer> playersByRace = getAGPlayersByRace(lookingForParty.getRace());
			if (lookingForParty.getMembers().size() + playersByRace.size() > getMaxPlayers(lookingForParty.getRace()))
				return AGQuestion.FAILED;

			registeredAGPlayers.putAll(lookingForParty.getMembers());
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
			if (getMaxPlayers(player.getRace()) <= 6)
				team = PlayerGroupService.createGroup(player, player, TeamType.AUTO_GROUP, 0);
			else
				team = PlayerAllianceService.createAlliance(player, player, TeamType.AUTO_ALLIANCE);
			int teamId = team.getObjectId();
			if (!instance.isRegistered(teamId))
				instance.register(teamId);
		} else {
			if (playersByRace.getFirst().isInGroup())
				PlayerGroupService.addPlayer(playersByRace.getFirst().getPlayerGroup(), player);
			else
				PlayerAllianceService.addPlayer(playersByRace.getFirst().getPlayerAlliance(), player);
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

	private int getMaxPlayers(Race race) {
		return DataManager.INSTANCE_COOLTIME_DATA.getMaxMemberCount(agt.getTemplate().getInstanceMapId(), race);
	}

	@Override
	public int getMaxPlayers() {
		return instance == null ? getMaxPlayers(Race.ASMODIANS) + getMaxPlayers(Race.ELYOS) : instance.getMaxPlayers();
	}
}
