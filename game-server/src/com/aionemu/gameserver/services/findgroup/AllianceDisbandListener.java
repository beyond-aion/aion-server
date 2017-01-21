package com.aionemu.gameserver.services.findgroup;

import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.alliance.callback.PlayerAllianceDisbandCallback;

/**
 * @author Rolandas
 */
public class AllianceDisbandListener extends PlayerAllianceDisbandCallback {

	@Override
	public void onBeforeAllianceDisband(PlayerAlliance alliance, boolean onBeforeOnly) {
		FindGroupService.getInstance().removeFindGroup(alliance.getRace(), 0, alliance.getTeamId());
	}

	@Override
	public void onAfterAllianceDisband(PlayerAlliance alliance, boolean onBeforeOnly) {
	}

}
