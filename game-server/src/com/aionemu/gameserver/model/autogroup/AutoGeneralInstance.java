package com.aionemu.gameserver.model.autogroup;

import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamType;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.model.templates.portal.PortalLoc;
import com.aionemu.gameserver.model.templates.portal.PortalPath;
import com.aionemu.gameserver.services.autogroup.AutoGroupUtility;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * This type should be created from FindGroupService / CM_FIND_GROUP. Unfortunately, "Instance Groups" content does not work properly, yet.
 * 
 * @author xTz
 */
public class AutoGeneralInstance extends AutoInstance {

	public AutoGeneralInstance(AutoGroupType agt) {
		super(agt);
	}

	@Override
	public AGQuestion addLookingForParty(LookingForParty lookingForParty) {
		super.writeLock();
		try {
			if (isRegistrationDisabled(lookingForParty) || lookingForParty.getMemberObjectIds().size() > 1 || registeredAGPlayers.size() >= getMaxPlayers())
				return AGQuestion.FAILED;
			AGPlayer agp = AutoGroupUtility.getNewAutoGroupPlayer(lookingForParty.getLeaderObjId());
			if (agp == null)
				return AGQuestion.FAILED;

			PlayerClass playerClass = agp.getPlayerClass();
			int clericSize = getAGPlayersByClass(PlayerClass.CLERIC).size();
			int templarSize = getAGPlayersByClass(PlayerClass.TEMPLAR).size();
			if (playerClass.equals(PlayerClass.CLERIC)) {
				if (clericSize > 0)
					return AGQuestion.FAILED;
			} else if (playerClass.equals(PlayerClass.TEMPLAR)) {
				if (templarSize > 0)
					return AGQuestion.FAILED;
			} else {
				int size = registeredAGPlayers.size();
				size -= clericSize;
				size -= templarSize;
				if (size >= 4) {
					return AGQuestion.FAILED;
				}
			}
			registeredAGPlayers.put(lookingForParty.getLeaderObjId(), agp);
			return instance == null && registeredAGPlayers.size() == getMaxPlayers() ? AGQuestion.READY : AGQuestion.ADDED;
		} finally {
			super.writeUnlock();
		}
	}

	@Override
	public void onEnterInstance(Player player) {
		super.onEnterInstance(player);
		List<Player> playersByRace = instance.getPlayersInside();
		if (playersByRace.size() == 1 && !playersByRace.get(0).isInGroup()) {
			PlayerGroup newGroup = PlayerGroupService.createGroup(playersByRace.get(0), player, TeamType.AUTO_GROUP, 0);
			int groupId = newGroup.getObjectId();
			if (!instance.isRegistered(groupId))
				instance.register(groupId);
		} else if (!playersByRace.isEmpty() && playersByRace.get(0).isInGroup()) {
			PlayerGroupService.addPlayer(playersByRace.get(0).getPlayerGroup(), player);
		}
		int objectId = player.getObjectId();
		if (!instance.isRegistered(objectId))
			instance.register(objectId);
	}

	@Override
	public void onPressEnter(Player player) {
		int worldId = instance.getMapId();
		PortalPath portal = DataManager.PORTAL2_DATA.getPortalDialogPath(worldId, DialogAction.SETPRO1, player);
		if (portal == null || portal.getRace() != player.getRace() && portal.getRace() != Race.PC_ALL)
			return;

		PortalLoc loc = DataManager.PORTAL_LOC_DATA.getPortalLoc(portal.getLocId());
		if (loc == null)
			return;

		TeleportService.teleportTo(player, worldId, instance.getInstanceId(), loc.getX(), loc.getY(), loc.getZ(), loc.getH());
		super.onPressEnter(player);
	}

	@Override
	public void onLeaveInstance(Player player) {
		super.unregister(player);
		PlayerGroupService.removePlayer(player);
	}
}
