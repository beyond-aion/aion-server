package com.aionemu.gameserver.model.team.alliance;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.GroupConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamType;
import com.aionemu.gameserver.model.team.alliance.events.*;
import com.aionemu.gameserver.model.team.alliance.events.AssignViceCaptainEvent.AssignType;
import com.aionemu.gameserver.model.team.common.events.PlayerLeavedEvent.LeaveReson;
import com.aionemu.gameserver.model.team.common.events.TeamCommand;
import com.aionemu.gameserver.model.team.common.events.TeamKinahDistributionEvent;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.team.common.legacy.PlayerAllianceEvent;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.model.team.league.events.LeagueLeftEvent;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.restrictions.PlayerRestrictions;
import com.aionemu.gameserver.services.VortexService;
import com.aionemu.gameserver.services.findgroup.FindGroupService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.TimeUtil;

/**
 * @author ATracer
 */
public class PlayerAllianceService {

	private static final Logger log = LoggerFactory.getLogger(PlayerAllianceService.class);
	private static final Map<Integer, PlayerAlliance> alliances = new ConcurrentHashMap<>();
	private static final AtomicBoolean offlineCheckStarted = new AtomicBoolean();

	public static final void inviteToAlliance(final Player inviter, Player invited) {
		if (PlayerRestrictions.canInviteToAlliance(inviter, invited)) {
			PlayerGroup playerGroup = invited.getPlayerGroup();

			if (playerGroup != null) {
				Player leader = playerGroup.getLeaderObject();
				if (!leader.equals(invited)) {
					PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_FORCE_INVITE_PARTY_HIM(invited.getName(), leader.getName()));
					PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_FORCE_INVITE_PARTY(leader.getName(), playerGroup.getMembers().size()));
					invited = leader;
				} else {
					PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_INVITED_HIS_PARTY(invited.getName()));
				}
			} else {
				PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_FORCE_INVITED_HIM(invited.getName()));
			}

			PlayerAllianceInvite invite = new PlayerAllianceInvite(inviter);
			if (invited.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_PARTY_ALLIANCE_DO_YOU_ACCEPT_HIS_INVITATION, invite)) {
				PacketSendUtility.sendPacket(invited,
					new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_PARTY_ALLIANCE_DO_YOU_ACCEPT_HIS_INVITATION, 0, 0, inviter.getName()));
			}
		}
	}

	public static PlayerAlliance createAlliance(Player leader, Player invited, TeamType type) {
		PlayerAlliance newAlliance = new PlayerAlliance(new PlayerAllianceMember(leader), type);
		alliances.put(newAlliance.getTeamId(), newAlliance);
		addPlayer(newAlliance, leader);
		addPlayer(newAlliance, invited);
		if (offlineCheckStarted.compareAndSet(false, true)) {
			initializeOfflineCheck();
		}
		return newAlliance;
	}

	private static void initializeOfflineCheck() {
		ThreadPoolManager.getInstance().scheduleAtFixedRate(new OfflinePlayerAllianceChecker(), 1000, 30 * 1000);
	}

	public static PlayerAllianceMember addPlayerToAlliance(PlayerAlliance alliance, Player invited) {
		PlayerAllianceMember member = new PlayerAllianceMember(invited);
		alliance.addMember(member);
		FindGroupService.getInstance().onJoinedTeam(invited);
		return member;
	}

	/**
	 * Change alliance's loot rules and notify team members
	 */
	public static final void changeGroupRules(PlayerAlliance alliance, LootGroupRules lootRules) {
		alliance.onEvent(new ChangeAllianceLootRulesEvent(alliance, lootRules));
	}

	/**
	 * Player entered world - search for non expired alliance
	 */
	public static final void onPlayerLogin(Player player) {
		for (PlayerAlliance alliance : alliances.values()) {
			PlayerAllianceMember member = alliance.getMember(player.getObjectId());
			if (member != null) {
				alliance.onEvent(new PlayerConnectedEvent(alliance, player));
			}
		}
	}

	/**
	 * Player leaved world - set last online on member
	 */
	public static final void onPlayerLogout(Player player) {
		PlayerAlliance alliance = player.getPlayerAlliance();
		if (alliance != null) {
			PlayerAllianceMember member = alliance.getMember(player.getObjectId());
			member.updateLastOnlineTime();
			alliance.onEvent(new PlayerDisconnectedEvent(alliance, player));
		}
	}

	/**
	 * Update alliance members to some event of player
	 */
	public static final void updateAlliance(Player player, PlayerAllianceEvent allianceEvent) {
		PlayerAlliance alliance = player.getPlayerAlliance();
		if (alliance != null) {
			alliance.onEvent(new PlayerAllianceUpdateEvent(alliance, player, allianceEvent));
		}
	}

	public static final void updateAllianceEffects(Player player, int slot) {
		PlayerAlliance alliance = player.getPlayerAlliance();
		if (alliance != null) {
			alliance.onEvent(new PlayerAllianceUpdateEvent(alliance, player, PlayerAllianceEvent.UPDATE_EFFECTS, slot));
		}
	}

	/**
	 * Add player to alliance
	 */
	public static final void addPlayer(PlayerAlliance alliance, Player player) {
		Objects.requireNonNull(alliance, "Alliance should not be null");
		alliance.onEvent(new PlayerAllianceEnteredEvent(alliance, player));
	}

	/**
	 * Remove player from alliance (normal leave, or kick offline player)
	 */
	public static final void removePlayer(Player player) {
		PlayerAlliance alliance = player.getPlayerAlliance();
		if (alliance != null) {
			if (alliance.getTeamType().isDefence()) {
				VortexService.getInstance().removeDefenderPlayer(player);
			}
			alliance.onEvent(new PlayerAllianceLeavedEvent(alliance, player));
		}
	}

	/**
	 * Remove player from alliance (ban)
	 */
	public static void banPlayer(Player bannedPlayer, Player banGiver) {
		Objects.requireNonNull(bannedPlayer, "Banned player should not be null");
		Objects.requireNonNull(banGiver, "Bangiver player should not be null");
		PlayerAlliance alliance = banGiver.getPlayerAlliance();
		if (alliance != null) {
			if (banGiver.equals(bannedPlayer)) {
				PacketSendUtility.sendPacket(banGiver, SM_SYSTEM_MESSAGE.STR_FORCE_CANT_BAN_SELF());
			} else if (!alliance.isLeader(banGiver)) {
				PacketSendUtility.sendPacket(banGiver, SM_SYSTEM_MESSAGE.STR_FORCE_ONLY_LEADER_CAN_BANISH());
			} else if (alliance.getTeamType() == TeamType.AUTO_ALLIANCE) {
				PacketSendUtility.sendPacket(banGiver, SM_SYSTEM_MESSAGE.STR_MSG_PARTY_FORCE_NO_RIGHT_TO_DECIDE());
			} else {
				if (alliance.getTeamType().isDefence())
					VortexService.getInstance().removeDefenderPlayer(bannedPlayer);
				if (alliance.hasMember(bannedPlayer.getObjectId()))
					alliance.onEvent(new PlayerAllianceLeavedEvent(alliance, bannedPlayer, LeaveReson.BAN, banGiver.getName()));
				else
					log.warn("TEAM: banning {} not in alliance {}", bannedPlayer, alliance.getMembers());
			}
		}
	}

	/**
	 * Disband alliance after minimum of members has been reached
	 */
	public static void disband(PlayerAlliance alliance, boolean onBefore) {
		FindGroupService.getInstance().removeRecruitment(alliance);
		League league = alliance.getLeague();
		if (onBefore && league != null)
			league.onEvent(new LeagueLeftEvent(league, alliance));
		alliance.onEvent(new AllianceDisbandEvent(alliance));
		alliances.remove(alliance.getTeamId());
		if (!onBefore && league != null)
			league.onEvent(new LeagueLeftEvent(league, alliance));
	}

	public static void changeLeader(Player player) {
		PlayerAlliance alliance = player.getPlayerAlliance();
		if (alliance != null) {
			alliance.onEvent(new ChangeAllianceLeaderEvent(alliance, player));
		}
	}

	/**
	 * Change vice captain position of player (promote, demote)
	 */
	public static void changeViceCaptain(Player player, AssignType assignType) {
		PlayerAlliance alliance = player.getPlayerAlliance();
		if (alliance != null) {
			alliance.onEvent(new AssignViceCaptainEvent(alliance, player, assignType));
		}
	}

	public static final PlayerAlliance searchAlliance(int playerObjId) {
		for (PlayerAlliance alliance : alliances.values()) {
			if (alliance.hasMember(playerObjId)) {
				return alliance;
			}
		}
		return null;
	}

	/**
	 * Move members between alliance groups
	 */
	public static void changeMemberGroup(Player player, int firstPlayer, int secondPlayer, int allianceGroupId) {
		PlayerAlliance alliance = player.getPlayerAlliance();
		if (alliance == null) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_YOU_ARE_NOT_FORCE_MEMBER());
			return;
		}
		if (alliance.isSomeCaptain(player)) {
			alliance.onEvent(new ChangeMemberGroupEvent(alliance, firstPlayer, secondPlayer, allianceGroupId));
		} else {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_FORCE_RIGHT_NOT_HAVE());
		}
	}

	/**
	 * Check that alliance is ready
	 */
	public static void checkReady(Player player, TeamCommand eventCode) {
		PlayerAlliance alliance = player.getPlayerAlliance();
		if (alliance != null) {
			alliance.onEvent(new CheckAllianceReadyEvent(alliance, player, eventCode));
		}
	}

	/**
	 * Share specific amount of kinah between alliance members
	 */
	public static void distributeKinah(Player player, long amount) {
		PlayerAlliance alliance = player.getPlayerAlliance();
		if (alliance != null) {
			alliance.onEvent(new TeamKinahDistributionEvent<>(alliance, player, amount));
		}
	}

	public static void distributeKinahInGroup(Player player, long amount) {
		PlayerAllianceGroup allianceGroup = player.getPlayerAllianceGroup();
		if (allianceGroup != null) {
			allianceGroup.onEvent(new TeamKinahDistributionEvent<>(allianceGroup, player, amount));
		}
	}

	public static class OfflinePlayerAllianceChecker implements Runnable {

		@Override
		public void run() {
			for (PlayerAlliance alliance : alliances.values()) {
				alliance.forEachTeamMember(member -> {
					int kickDelay = alliance.getTeamType().isAutoTeam() ? 60 : GroupConfig.ALLIANCE_REMOVE_TIME;
					if (!member.isOnline() && TimeUtil.isExpired(member.getLastOnlineTime() + kickDelay * 1000)) {
						if (alliance.getTeamType().isOffence()) {
							VortexService.getInstance().removeInvaderPlayer(member.getObject());
						}
						alliance.onEvent(new PlayerAllianceLeavedEvent(alliance, member.getObject(), LeaveReson.LEAVE_TIMEOUT));
					}
				});
			}
		}

	}

}
