package com.aionemu.gameserver.model.team.league;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.commons.callbacks.util.GlobalCallbackHelper;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.team.common.legacy.LootRuleType;
import com.aionemu.gameserver.model.team.league.events.LeagueChangeLeaderEvent;
import com.aionemu.gameserver.model.team.league.events.LeagueCreateEvent;
import com.aionemu.gameserver.model.team.league.events.LeagueDisbandEvent;
import com.aionemu.gameserver.model.team.league.events.LeagueInviteEvent;
import com.aionemu.gameserver.model.team.league.events.LeagueJoinEvent;
import com.aionemu.gameserver.model.team.league.events.LeagueKinahDistributionEvent;
import com.aionemu.gameserver.model.team.league.events.LeagueLeftEvent;
import com.aionemu.gameserver.model.team.league.events.LeagueLeftEvent.LeaveReson;
import com.aionemu.gameserver.model.team.league.events.LeagueLootRulesChangeEvent;
import com.aionemu.gameserver.model.team.league.events.LeagueMoveEvent;
import com.aionemu.gameserver.model.team.league.events.LeagueShowBrandEvent;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.google.common.base.Preconditions;

/**
 * @author ATracer
 */
public class LeagueService {

	private static final Map<Integer, League> leagues = new ConcurrentHashMap<>();

	static {
		GlobalCallbackHelper.addCallback(new AllianceDisbandListener());
	}

	public static final void inviteToLeague(final Player inviter, Player invited) {
		if (canInvite(inviter, invited)) {
			PlayerAlliance playerAlliance = invited.getPlayerAlliance();

			if (playerAlliance != null) {
				Player leader = playerAlliance.getLeaderObject();
				if (!leader.equals(invited)) {
					PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_UNION_INVITE_HIS_LEADER(invited.getName(), leader.getName()));
				}
				invited = leader;
			}

			LeagueInviteEvent invite = new LeagueInviteEvent(inviter, invited);
			if (invited.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_MSGBOX_UNION_INVITE_ME, invite)) {
				PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_UNION_INVITE_HIM(invited.getName(), invited.getPlayerAlliance().size()));
				PacketSendUtility.sendPacket(invited, new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_MSGBOX_UNION_INVITE_ME, 0, 0, inviter.getName()));
			}
		}
	}

	public static final boolean canInvite(Player inviter, Player invited) {
		if (inviter.getLifeStats().isAlreadyDead()) {
			// You cannot use the Alliance League invitation function while you are dead.
			PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_UNION_CANT_INVITE_WHEN_DEAD());
			return false;
		} else if (!invited.isOnline()) {
			// The player you invited to the Alliance League is currently offline.
			PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_UNION_OFFLINE_MEMBER());
			return false;
		} else if (invited.getPlayerAlliance() == null) {
			// Currently, %0 cannot accept your invitation to join the alliance.
			PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_UNION_CANT_INVITE_WHEN_HE_IS_ASKED_QUESTION(invited.getName()));
			return false;
		} else if (inviter.getPlayerAlliance().hasMember(invited.getObjectId())) {
			// You cannot invite your own alliance.
			PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_UNION_CANT_INVITE_SELF());
			return false;
		} else if (invited.getPlayerAlliance().isInLeague()) {
			// The selected target is already a member of another force league.
			PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_UNION_ALREADY_MY_UNION());
			return false;
		} else if (inviter.getPlayerAlliance().isInLeague() && inviter.getPlayerAlliance().getLeague().isFull()) {
			// You cannot invite anymore as the Alliance League is full.
			PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_UNION_CANT_ADD_NEW_MEMBER());
			return false;
		} else if (inviter.getPlayerAlliance().isInLeague() && invited.getPlayerAlliance().isInLeague()
			&& inviter.getPlayerAlliance().getLeague().equals(invited.getPlayerAlliance().getLeague())) {
			// %0 is already a member of another Alliance League.
			PacketSendUtility.sendPacket(inviter, SM_SYSTEM_MESSAGE.STR_UNION_ALREADY_OTHER_UNION(invited.getName()));
			return false;
		}
		return true;
	}

	public static final League createLeague(Player leader) {
		PlayerAlliance alliance = leader.getPlayerAlliance();
		Objects.requireNonNull(alliance, "Alliance can not be null");
		LeagueMember mainAlliance = new LeagueMember(alliance, 0);
		League league = new League(mainAlliance);
		league.setLootGroupRules(new LootGroupRules(LootRuleType.FREEFORALL, 0, 0, 2, 2, 2, 2, 2));
		league.addMember(mainAlliance);
		leagues.put(league.getTeamId(), league);
		league.onEvent(new LeagueCreateEvent(league));
		return league;
	}

	/**
	 * Add alliance to league
	 */
	public static final void addAlliance(League league, PlayerAlliance alliance) {
		Objects.requireNonNull(league, "League should not be null");
		league.onEvent(new LeagueJoinEvent(league, alliance));
	}

	/**
	 * Remove alliance from league (normal leave)
	 */
	public static final void removeAlliance(PlayerAlliance alliance) {
		if (alliance != null) {
			League league = alliance.getLeague();
			Objects.requireNonNull(league, "League should not be null");
			league.onEvent(new LeagueLeftEvent(league, alliance, LeaveReson.LEAVE));
		}
	}

	/**
	 * Remove alliance from league (expel)
	 */
	public static final void expelAlliance(Player expelledPlayer, Player expelGiver) {
		Objects.requireNonNull(expelledPlayer, "Expelled player should not be null");
		Objects.requireNonNull(expelGiver, "ExpelGiver player should not be null");
		Preconditions.checkArgument(expelGiver.isInLeague(), "Expelled player should be in league");
		Preconditions.checkArgument(expelledPlayer.isInLeague(), "ExpelGiver should be in league");
		Preconditions.checkArgument(expelGiver.getPlayerAlliance().getLeague().isLeader(expelGiver.getPlayerAlliance()),
			"ExpelGiver alliance should be the leader of league");
		Preconditions.checkArgument(expelGiver.getPlayerAlliance().isLeader(expelGiver), "ExpelGiver should be the leader of alliance");
		PlayerAlliance alliance = expelGiver.getPlayerAlliance();
		League league = alliance.getLeague();
		league.onEvent(new LeagueLeftEvent(league, expelledPlayer.getPlayerAlliance(), LeaveReson.EXPEL));
	}

	public static void setLeader(Player player, Player teamSubjective) {
		PlayerAlliance alliance = player.getPlayerAlliance();
		if (alliance != null) {
			League league = alliance.getLeague();
			if (league != null) {
				league.onEvent(new LeagueChangeLeaderEvent(alliance, teamSubjective));
			}
		}
	}

	/**
	 * Disband league after minimum of members has been reached
	 */
	public static void disband(League league) {
		Preconditions.checkState(league.onlineMembers() <= 1, "Can't disband league with more than one online member");
		league.onEvent(new LeagueDisbandEvent(league));
		leagues.remove(league.getTeamId());
	}

	public static Collection<League> getLeagues() {
		return Collections.unmodifiableCollection(leagues.values());
	}

	public static void showBrand(Player player, int targetObjId, int brandId) {
		League league = player.getPlayerAlliance().getLeague();
		league.onEvent(new LeagueShowBrandEvent(league, targetObjId, brandId));
	}

	public static void moveAlliance(Player player, int selectedId, int targetId) {
		League league = player.getPlayerAlliance().getLeague();
		if (league.getLeaderObject().getLeaderObject().equals(player)) {
			league.onEvent(new LeagueMoveEvent(league, selectedId, targetId));
		}
	}

	public static final void changeGroupRules(League league, LootGroupRules lootRules) {
		league.onEvent(new LeagueLootRulesChangeEvent(league, lootRules));
	}

	public static void distributeKinah(Player player, long amount) {
		League league = player.getPlayerAlliance().getLeague();
		if (league != null) {
			league.onEvent(new LeagueKinahDistributionEvent(player, amount));
		}
	}

}
