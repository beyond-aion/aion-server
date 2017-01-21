package com.aionemu.gameserver.services.findgroup;

import com.aionemu.gameserver.model.gameobjects.FindGroup;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.group.callback.PlayerGroupCreateCallback;

/**
 * @author Rolandas
 */
public class PlayerGroupCreateListener extends PlayerGroupCreateCallback {

	@Override
	public void onBeforeGroupCreate(Player player) {
	}

	@Override
	public void onAfterGroupCreate(Player player) {
		FindGroup inviterFindGroup = FindGroupService.getInstance().removeFindGroup(player.getRace(), 0x00, player.getObjectId());
		if (inviterFindGroup == null)
			inviterFindGroup = FindGroupService.getInstance().removeFindGroup(player.getRace(), 0x04, player.getObjectId());
		if (inviterFindGroup != null)
			FindGroupService.getInstance().addFindGroupList(player, 0x02, inviterFindGroup.getMessage(), inviterFindGroup.getGroupType());
	}

}
