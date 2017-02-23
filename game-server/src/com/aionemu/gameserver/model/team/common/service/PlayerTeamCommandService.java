package com.aionemu.gameserver.model.team.common.service;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.alliance.events.AssignViceCaptainEvent.AssignType;
import com.aionemu.gameserver.model.team.common.events.TeamCommand;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.model.team.league.LeagueMember;
import com.aionemu.gameserver.model.team.league.LeagueService;
import com.google.common.base.Preconditions;

/**
 * @author ATracer
 */
public class PlayerTeamCommandService {

	public static final void executeCommand(Player player, TeamCommand command, int playerObjId) {
		Player teamSubjective = getTeamSubjective(player, playerObjId);
		// if playerObjId is not 0 - subjective should not be active player
		Preconditions.checkArgument(playerObjId == 0 || command == TeamCommand.LEAGUE_SET_LEADER || teamSubjective.getObjectId() == playerObjId
			|| command == TeamCommand.LEAGUE_EXPEL, "Wrong command detected " + command);
		execute(player, command, teamSubjective);
	}

	private static final void execute(Player player, TeamCommand eventCode, Player teamSubjective) {
		switch (eventCode) {
			case GROUP_BAN_MEMBER:
				PlayerGroupService.banPlayer(teamSubjective, player);
				break;
			case GROUP_SET_LEADER:
				PlayerGroupService.changeLeader(teamSubjective);
				break;
			case GROUP_REMOVE_MEMBER:
				PlayerGroupService.removePlayer(teamSubjective);
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
				PlayerAllianceService.banPlayer(teamSubjective, player);
				break;
			case ALLIANCE_SET_CAPTAIN:
				PlayerAllianceService.changeLeader(teamSubjective);
				break;
			case ALLIANCE_CHECKREADY_CANCEL:
			case ALLIANCE_CHECKREADY_START:
			case ALLIANCE_CHECKREADY_AUTOCANCEL:
			case ALLIANCE_CHECKREADY_NOTREADY:
			case ALLIANCE_CHECKREADY_READY:
				PlayerAllianceService.checkReady(player, eventCode);
				break;
			case ALLIANCE_SET_VICECAPTAIN:
				PlayerAllianceService.changeViceCaptain(teamSubjective, AssignType.PROMOTE);
				break;
			case ALLIANCE_UNSET_VICECAPTAIN:
				PlayerAllianceService.changeViceCaptain(teamSubjective, AssignType.DEMOTE);
				break;
			case LEAGUE_LEAVE:
				LeagueService.removeAlliance(player.getPlayerAlliance());
				break;
			case LEAGUE_EXPEL:
				LeagueService.expelAlliance(teamSubjective, player);
				break;
			case LEAGUE_SET_LEADER:
				LeagueService.setLeader(player, teamSubjective);
				break;
		}
	}

	private static final Player getTeamSubjective(Player player, int playerObjId) {
		if (playerObjId == 0) {
			return player;
		}
		if (player.isInTeam()) {
			TeamMember<Player> member = player.getCurrentTeam().getMember(playerObjId);
			if (member != null) {
				return member.getObject();
			}
			if (player.isInLeague()) {
				LeagueMember subjective = player.getPlayerAlliance().getLeague().getMember(playerObjId);
				if (subjective != null) {
					return subjective.getObject().getLeaderObject();
				}
			}
		}
		return player;
	}
}
