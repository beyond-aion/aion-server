package com.aionemu.gameserver.model.team.league;

import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;

/**
 * @author ATracer
 */
public class LeagueMember implements TeamMember<PlayerAlliance> {

	private final PlayerAlliance alliance;
	private int leaguePosition;

	public LeagueMember(PlayerAlliance alliance, int position) {
		this.alliance = alliance;
		this.leaguePosition = position;
	}

	@Override
	public int getObjectId() {
		return alliance.getObjectId();
	}

	@Override
	public String getName() {
		return alliance.getName();
	}

	@Override
	public PlayerAlliance getObject() {
		return alliance;
	}

	public void setLeaguePosition(int leaguePosition) {
		this.leaguePosition = leaguePosition;
	}

	public final int getLeaguePosition() {
		return leaguePosition;
	}

}
