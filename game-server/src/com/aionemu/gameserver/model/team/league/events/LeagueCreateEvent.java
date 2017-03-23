package com.aionemu.gameserver.model.team.league.events;

import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;

/**
 * @author ATracer
 */
public class LeagueCreateEvent extends AlwaysTrueTeamEvent {

	private final League league;

	public LeagueCreateEvent(League league) {
		this.league = league;
	}

	@Override
	public void handleEvent() {
		league.forEach(alliance -> {
			alliance.sendPackets(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_ALLIANCE_ENTERED, alliance.getLeader().getName()));
			alliance.sendPackets(new SM_SHOW_BRAND(0, 0, true));
		});
	}

}
