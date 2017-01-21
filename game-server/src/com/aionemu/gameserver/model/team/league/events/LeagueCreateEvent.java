package com.aionemu.gameserver.model.team.league.events;

import com.aionemu.gameserver.model.team.TeamEvent;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.model.team.league.LeagueMember;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;
import com.google.common.base.Predicate;

/**
 * @author ATracer
 */
public class LeagueCreateEvent implements Predicate<LeagueMember>, TeamEvent {

	private final League league;

	public LeagueCreateEvent(League league) {
		this.league = league;
	}

	@Override
	public boolean checkCondition() {
		return true;
	}

	@Override
	public void handleEvent() {
		league.apply(this);
	}

	@Override
	public boolean apply(LeagueMember member) {
		PlayerAlliance alliance = member.getObject();
		alliance.sendPacket(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_ALLIANCE_ENTERED, alliance.getLeader().getName()));
		alliance.sendPacket(new SM_SHOW_BRAND(0, 0, true));
		return true;
	}

}
