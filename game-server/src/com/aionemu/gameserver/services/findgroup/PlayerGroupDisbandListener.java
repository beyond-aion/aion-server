package com.aionemu.gameserver.services.findgroup;

import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.team.group.callback.PlayerGroupDisbandCallback;

/**
 * @author Rolandas
 */
public class PlayerGroupDisbandListener extends PlayerGroupDisbandCallback {

	@Override
	public void onBeforeGroupDisband(PlayerGroup group) {
		FindGroupService.getInstance().removeFindGroup(group.getRace(), 0, group.getTeamId());
	}

	@Override
	public void onAfterGroupDisband(PlayerGroup group) {
	}
}
