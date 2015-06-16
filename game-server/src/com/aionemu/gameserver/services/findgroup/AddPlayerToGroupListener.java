package com.aionemu.gameserver.services.findgroup;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.group.PlayerGroup;
import com.aionemu.gameserver.model.team2.group.callback.AddPlayerToGroupCallback;

/**
 * @author Rolandas
 */
public class AddPlayerToGroupListener extends AddPlayerToGroupCallback {

	@Override
	public void onBeforePlayerAddToGroup(PlayerGroup group, Player player) {
		FindGroupService.getInstance().removeFindGroup(player.getRace(), 0x00, player.getObjectId());
		FindGroupService.getInstance().removeFindGroup(player.getRace(), 0x04, player.getObjectId());
	}

	@Override
	public void onAfterPlayerAddToGroup(PlayerGroup group, Player player) {
		if (group.isFull()) {
			FindGroupService.getInstance().removeFindGroup(group.getRace(), 0, group.getObjectId());
		}
	}
}
