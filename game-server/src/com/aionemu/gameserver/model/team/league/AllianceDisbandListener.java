package com.aionemu.gameserver.model.team.league;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.alliance.callback.PlayerAllianceDisbandCallback;
import com.aionemu.gameserver.model.team.league.events.LeagueLeftEvent;

/**
 * @author Rolandas
 */
public class AllianceDisbandListener extends PlayerAllianceDisbandCallback {

	private static final Logger log = LoggerFactory.getLogger(AllianceDisbandListener.class);

	@Override
	public void onBeforeAllianceDisband(PlayerAlliance alliance, boolean onBeforeOnly) {
		try {
			if (onBeforeOnly) {
				for (League league : LeagueService.getLeagues()) {
					if (league.hasMember(alliance.getTeamId())) {
						league.onEvent(new LeagueLeftEvent(league, alliance));
					}
				}
			}
		} catch (Throwable t) {
			log.error("Error during alliance disband listen", t);
		}
	}

	@Override
	public void onAfterAllianceDisband(PlayerAlliance alliance, boolean onBeforeOnly) {
		try {
			if (!onBeforeOnly) {
				for (League league : LeagueService.getLeagues()) {
					if (league.hasMember(alliance.getTeamId())) {
						league.onEvent(new LeagueLeftEvent(league, alliance));
					}
				}
			}
		} catch (Throwable t) {
			log.error("Error during alliance disband listen", t);
		}
	}

}
