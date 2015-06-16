package com.aionemu.gameserver.services.findgroup;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.alliance.callback.AddPlayerToAllianceCallback;

/**
 * @author Rolandas
 */
public class AddPlayerToAllianceListener extends AddPlayerToAllianceCallback {

	@Override
	public void onBeforePlayerAddToAlliance(PlayerAlliance alliance, Player player) {
		FindGroupService.getInstance().removeFindGroup(player.getRace(), 0x00, player.getObjectId());
		FindGroupService.getInstance().removeFindGroup(player.getRace(), 0x04, player.getObjectId());
	}

	@Override
	public void onAfterPlayerAddToAlliance(PlayerAlliance alliance, Player player) {
		if (alliance.isFull())
			FindGroupService.getInstance().removeFindGroup(alliance.getRace(), 0, alliance.getObjectId());
	}
}
