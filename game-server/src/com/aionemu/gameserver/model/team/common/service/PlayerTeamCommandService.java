package com.aionemu.gameserver.model.team.common.service;

import java.util.Objects;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.alliance.events.AssignViceCaptainEvent.AssignType;
import com.aionemu.gameserver.model.team.common.events.TeamCommand;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.model.team.league.LeagueMember;
import com.aionemu.gameserver.model.team.league.LeagueService;

/**
 * @author ATracer
 */
public class PlayerTeamCommandService {

	public static void executeCommand(Player player, TeamCommand command, int memberObjId) {
		TemporaryPlayerTeam<? extends TeamMember<Player>> team = player.getCurrentTeam();
		if (team == null) // team might have been disbanded or player can have been kicked out of his team by the time the packet arrived 
			return;
		switch (command) {
			case GROUP_BAN_MEMBER:
				PlayerGroupService.banPlayer(findMember(team, player, memberObjId), player);
				break;
			case GROUP_SET_LEADER:
				PlayerGroupService.changeLeader(findMember(team, player, memberObjId));
				break;
			case GROUP_REMOVE_MEMBER:
				PlayerGroupService.removePlayer(findMember(team, player, memberObjId));
				break;
			case GROUP_START_MENTORING:
				PlayerGroupService.startMentoring(player);
				break;
			case GROUP_END_MENTORING:
				PlayerGroupService.stopMentoring(player);
				break;
			case ALLIANCE_LEAVE:
				PlayerAllianceService.removePlayer(player);
				break;
			case ALLIANCE_BAN_MEMBER:
				PlayerAllianceService.banPlayer(findMember(team, player, memberObjId), player);
				break;
			case ALLIANCE_SET_CAPTAIN:
				PlayerAllianceService.changeLeader(findMember(team, player, memberObjId));
				break;
			case ALLIANCE_CHECKREADY_CANCEL:
			case ALLIANCE_CHECKREADY_START:
			case ALLIANCE_CHECKREADY_AUTOCANCEL:
			case ALLIANCE_CHECKREADY_NOTREADY:
			case ALLIANCE_CHECKREADY_READY:
				PlayerAllianceService.checkReady(player, command);
				break;
			case ALLIANCE_SET_VICECAPTAIN:
				PlayerAllianceService.changeViceCaptain(findMember(team, player, memberObjId), AssignType.PROMOTE);
				break;
			case ALLIANCE_UNSET_VICECAPTAIN:
				PlayerAllianceService.changeViceCaptain(findMember(team, player, memberObjId), AssignType.DEMOTE);
				break;
			case LEAGUE_LEAVE:
				LeagueService.removeAlliance(player.getPlayerAlliance());
				break;
			case LEAGUE_EXPEL:
				LeagueService.expelAlliance(findLeagueAlliance(team, player, memberObjId), player);
				break;
			case LEAGUE_SET_LEADER:
				PlayerAlliance leagueAlliance = findLeagueAlliance(team, player, memberObjId).getObject();
				LeagueService.setLeader(player, leagueAlliance.getLeaderObject());
				break;
		}
	}

	private static LeagueMember findLeagueAlliance(TemporaryPlayerTeam<? extends TeamMember<Player>> team, Player player, int leagueAllianceId) {
		League league = team instanceof PlayerAlliance ? ((PlayerAlliance) team).getLeague() : null;
		Objects.requireNonNull(league, () -> player + " tried to execute league command without an active league alliance");
		return Objects.requireNonNull(league.getMember(leagueAllianceId),
			() -> player + " tried to execute league command on invalid alliance " + leagueAllianceId);
	}

	private static Player findMember(TemporaryPlayerTeam<? extends TeamMember<Player>> team, Player player, int memberObjId) {
		if (memberObjId == 0)
			return player;
		TeamMember<Player> member = team.getMember(memberObjId);
		Objects.requireNonNull(member, () -> player + " tried to execute team command on non-existent member with ID " + memberObjId);
		return member.getObject();
	}
}
