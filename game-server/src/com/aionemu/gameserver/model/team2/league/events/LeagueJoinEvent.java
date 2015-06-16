package com.aionemu.gameserver.model.team2.league.events;

import com.aionemu.gameserver.model.team2.TeamEvent;
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.league.League;
import com.aionemu.gameserver.model.team2.league.LeagueMember;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;
import com.google.common.base.Predicate;

/**
 *
 * @author Tibald
 */
public class LeagueJoinEvent implements Predicate<LeagueMember>, TeamEvent {

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
		league.apply(this);
	}

	@Override
	public boolean apply(LeagueMember member) {
		PlayerAlliance alliance = member.getObject();
		if (alliance.equals(invitedAlliance)) {
			alliance.sendPacket(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_ALLIANCE_ENTERED, league.getCaptain().getName()));
		}
		else {
			alliance.sendPacket(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_JOINED_ALLIANCE, invitedAlliance.getLeaderObject().getName()));
		}
		alliance.sendPacket(new SM_SHOW_BRAND(0, 0, true));
		return true;
	}

}