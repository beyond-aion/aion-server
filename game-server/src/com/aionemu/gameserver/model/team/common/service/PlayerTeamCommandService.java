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
			case GROUP_BAN_MEMBER: {
				if (!team.isLeader(player))
					return;
				PlayerGroupService.banPlayer(findMember(team, player, memberObjId), player);
				break;
			}
			case GROUP_SET_LEADER: {
				if (!team.isLeader(player))
					return;
				Player newLeader = findMember(team, player, memberObjId);
				if (!team.isLeader(newLeader))
					PlayerGroupService.changeLeader(newLeader);
				break;
			}
			case GROUP_REMOVE_MEMBER: {
				// This is the client's "leave group"; kicking another member is GROUP_BAN_MEMBER.
				if (memberObjId != 0 && memberObjId != player.getObjectId())
					return;
				PlayerGroupService.removePlayer(findMember(team, player, memberObjId));
				break;
			}
			case GROUP_START_MENTORING:
				PlayerGroupService.startMentoring(player);
				break;
			case GROUP_END_MENTORING:
				PlayerGroupService.stopMentoring(player);
				break;
			case ALLIANCE_LEAVE:
				PlayerAllianceService.removePlayer(player);
				break;
			case ALLIANCE_BAN_MEMBER: {
				PlayerAllianceService.banPlayer(findMember(team, player, memberObjId), player);
				break;
			}
			case ALLIANCE_SET_CAPTAIN: {
				if (!team.isLeader(player))
					return;
				Player newCaptain = findMember(team, player, memberObjId);
				if (!team.isLeader(newCaptain))
					PlayerAllianceService.changeLeader(newCaptain);
				break;
			}
			case ALLIANCE_CHECKREADY_CANCEL:
			case ALLIANCE_CHECKREADY_START:
			case ALLIANCE_CHECKREADY_AUTOCANCEL:
			case ALLIANCE_CHECKREADY_NOTREADY:
			case ALLIANCE_CHECKREADY_READY:
				PlayerAllianceService.checkReady(player, command);
				break;
			case ALLIANCE_SET_VICECAPTAIN:
			case ALLIANCE_UNSET_VICECAPTAIN: {
				if (!team.isLeader(player))
					return;
				PlayerAllianceService.changeViceCaptain(findMember(team, player, memberObjId),
					command == TeamCommand.ALLIANCE_SET_VICECAPTAIN ? AssignType.PROMOTE : AssignType.DEMOTE);
				break;
			}
			case LEAGUE_LEAVE: {
				// Only the leader of the alliance may take it out of the league.
				PlayerAlliance alliance = player.getPlayerAlliance();
				if (!team.isLeader(player) || alliance == null || alliance.getLeague() == null)
					return;
				LeagueService.removeAlliance(alliance);
				break;
			}
			case LEAGUE_EXPEL:
				LeagueService.expelAlliance(findLeagueAlliance(team, player, memberObjId), player);
				break;
			case LEAGUE_SET_LEADER: {
				PlayerAlliance ownAlliance = player.getPlayerAlliance();
				if (ownAlliance == null || ownAlliance.getLeague() == null || !ownAlliance.getLeague().isLeader(ownAlliance))
					return;
				PlayerAlliance leagueAlliance = findLeagueAlliance(team, player, memberObjId).getObject();
				LeagueService.setLeader(player, leagueAlliance.getLeaderObject());
				break;
			}
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
