package com.aionemu.gameserver.model.team.league.events;

import com.aionemu.gameserver.model.team.TeamEvent;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.model.team.league.LeagueMember;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;

/**
 * @author Tibald
 */
public class LeagueJoinEvent implements TeamEvent {

	private final League league;
	private final PlayerAlliance invitedAlliance;

	public LeagueJoinEvent(League league, PlayerAlliance invitedAlliance) {
		this.league = league;
		this.invitedAlliance = invitedAlliance;
	}

	/**
	 * Entered alliance should not be in league yet
	 */
	@Override
	public boolean checkCondition() {
		return !league.hasMember(invitedAlliance.getObjectId());
	}

	@Override
	public void handleEvent() {
		league.addMember(new LeagueMember(invitedAlliance, league.size()));
		league.forEach(alliance -> {
			if (alliance.equals(invitedAlliance)) {
				alliance.sendPackets(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_ALLIANCE_ENTERED, league.getCaptain().getName()));
			} else {
				alliance.sendPackets(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_JOINED_ALLIANCE, invitedAlliance.getLeaderObject().getName()));
			}
		});
	}

}
