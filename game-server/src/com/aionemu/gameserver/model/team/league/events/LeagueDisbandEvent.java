package com.aionemu.gameserver.model.team.league.events;

import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.model.team.league.events.LeagueLeftEvent.LeaveReson;

/**
 * @author ATracer
 */
public class LeagueDisbandEvent extends AlwaysTrueTeamEvent {

	private final League league;

	public LeagueDisbandEvent(League league) {
		this.league = league;
	}

	@Override
	public void handleEvent() {
		league.forEach(alliance -> league.onEvent(new LeagueLeftEvent(league, alliance, LeaveReson.DISBAND)));
	}

}
