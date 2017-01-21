package com.aionemu.gameserver.model.team.league.events;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.model.team.league.LeagueMember;
import com.aionemu.gameserver.model.team.league.LeagueService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.google.common.base.Predicate;

/**
 * @author ATracer
 */
public class LeagueLeftEvent extends AlwaysTrueTeamEvent implements Predicate<LeagueMember> {

	private final League league;
	private final PlayerAlliance alliance;
	private final LeaveReson reason;

	public static enum LeaveReson {

		LEAVE,
		EXPEL,
		DISBAND;

	}

	public LeagueLeftEvent(League league, PlayerAlliance alliance) {
		this(league, alliance, LeaveReson.LEAVE);
	}

	public LeagueLeftEvent(League league, PlayerAlliance alliance, LeaveReson reason) {
		this.league = league;
		this.alliance = alliance;
		this.reason = reason;
	}

	@Override
	public void handleEvent() {
		league.removeMember(alliance.getTeamId());
		Player newLeader = league.reorganize();
		league.apply(this);

		if (newLeader != null) {
			league.sendPacket(SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_LEADER_TIMEOUT(newLeader.getName(), StringUtils.EMPTY, StringUtils.EMPTY));
		}

		switch (reason) {
			case LEAVE:
				alliance.sendPacket(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_LEFT_ME, alliance.getLeaderObject().getName()));
				alliance.sendPacket(new SM_SHOW_BRAND(0, 0, alliance.isInLeague()));
				checkDisband();
				break;
			case EXPEL:
				// TODO getCaptainName in team
				alliance.sendPacket(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_EXPELLED, league.getLeaderObject().getLeader().getName()));
				alliance.sendPacket(new SM_SHOW_BRAND(0, 0, alliance.isInLeague()));
				checkDisband();
				break;
			case DISBAND:
				alliance.sendPacket(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_DISPERSED, StringUtils.EMPTY));
				alliance.sendPacket(new SM_SHOW_BRAND(0, 0, alliance.isInLeague()));
				break;
		}
	}

	private void checkDisband() {
		if (league.onlineMembers() <= 1) {
			LeagueService.disband(league);
		}
	}

	@Override
	public boolean apply(LeagueMember member) {
		final PlayerAlliance leagueAlliance = member.getObject();
		leagueAlliance.applyOnMembers(new Predicate<Player>() {

			@Override
			public boolean apply(Player member) {
				switch (reason) {
					case LEAVE:
						PacketSendUtility.sendPacket(member, new SM_ALLIANCE_INFO(leagueAlliance, SM_ALLIANCE_INFO.LEAGUE_LEFT_HIM, alliance.getLeader()
							.getName()));
						PacketSendUtility.sendPacket(member, new SM_SHOW_BRAND(0, 0, leagueAlliance.isInLeague()));
						break;
					case EXPEL:
						// TODO may be EXPEL message only to leader
						PacketSendUtility.sendPacket(member, new SM_ALLIANCE_INFO(leagueAlliance, SM_ALLIANCE_INFO.LEAGUE_EXPEL, alliance.getLeader().getName()));
						PacketSendUtility.sendPacket(member, new SM_SHOW_BRAND(0, 0, leagueAlliance.isInLeague()));
						break;
				}
				return true;
			}

		});

		return true;
	}

}
