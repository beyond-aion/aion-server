package com.aionemu.gameserver.model.team.league.events;

import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;

/**
 * @author Tibald
 */
public class LeagueShowBrandEvent extends AlwaysTrueTeamEvent {

	private final League league;
	private final int targetObjId;
	private final int brandId;

	public LeagueShowBrandEvent(League league, int targetObjId, int brandId) {
		this.league = league;
		this.targetObjId = targetObjId;
		this.brandId = brandId;
	}

	@Override
	public void handleEvent() {
		league.forEach(alliance -> alliance.sendPackets(new SM_SHOW_BRAND(brandId, targetObjId, true)));
	}

}
