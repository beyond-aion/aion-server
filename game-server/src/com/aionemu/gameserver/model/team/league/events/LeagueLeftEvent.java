package com.aionemu.gameserver.model.team.league.events;

import java.util.function.Consumer;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.common.events.AlwaysTrueTeamEvent;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.model.team.league.LeagueService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class LeagueLeftEvent extends AlwaysTrueTeamEvent implements Consumer<PlayerAlliance> {

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
		league.forEach(this);

		if (newLeader != null) {
			league.sendPackets(SM_SYSTEM_MESSAGE.STR_UNION_CHANGE_LEADER_TIMEOUT(newLeader.getName()));
		}

		switch (reason) {
			case LEAVE:
				alliance.sendPackets(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_LEFT_ME, alliance.getLeaderObject().getName()));
				checkDisband();
				break;
			case EXPEL:
				// TODO getCaptainName in team
				alliance.sendPackets(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_EXPELLED, league.getLeaderObject().getLeader().getName()));
				checkDisband();
				break;
			case DISBAND:
				alliance.sendPackets(new SM_ALLIANCE_INFO(alliance, SM_ALLIANCE_INFO.LEAGUE_DISPERSED, ""));
				break;
		}
	}

	private void checkDisband() {
		if (league.shouldDisband()) {
			LeagueService.disband(league);
		}
	}

	@Override
	public void accept(PlayerAlliance leagueAlliance) {
		leagueAlliance.forEach(member -> {
			switch (reason) {
				case LEAVE:
					PacketSendUtility.sendPacket(member,
						new SM_ALLIANCE_INFO(leagueAlliance, SM_ALLIANCE_INFO.LEAGUE_LEFT_HIM, alliance.getLeader().getName()));
					break;
				case EXPEL:
					// TODO may be EXPEL message only to leader
					PacketSendUtility.sendPacket(member, new SM_ALLIANCE_INFO(leagueAlliance, SM_ALLIANCE_INFO.LEAGUE_EXPEL, alliance.getLeader().getName()));
					break;
			}
		});
	}

}
