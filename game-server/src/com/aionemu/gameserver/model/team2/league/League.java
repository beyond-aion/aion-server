package com.aionemu.gameserver.model.team2.league;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.GeneralTeam;
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceMember;
import com.aionemu.gameserver.model.team2.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.team2.group.PlayerFilters;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SHOW_BRAND;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;

/**
 * @author ATracer
 */
public class League extends GeneralTeam<PlayerAlliance, LeagueMember> {

	private LootGroupRules lootGroupRules = new LootGroupRules();
	private static final LeagueMemberComparator MEMBER_COMPARATOR = new LeagueMemberComparator();

	public League(LeagueMember leader) {
		super(IDFactory.getInstance().nextId());
		initializeTeam(leader);
	}

	protected final void initializeTeam(LeagueMember leader) {
		setLeader(leader);
	}

	@Override
	public Collection<PlayerAlliance> getOnlineMembers() {
		return getMembers();
	}

	@Override
	public void addMember(LeagueMember member) {
		super.addMember(member);
		member.getObject().setLeague(this);
	}

	@Override
	public void removeMember(LeagueMember member) {
		super.removeMember(member);
		member.getObject().setLeague(null);
	}

	@Override
	public void sendPacket(AionServerPacket packet) {
		for (PlayerAlliance alliance : getMembers()) {
			alliance.sendPacket(packet);
		}
	}

	@Override
	public void sendPacket(AionServerPacket packet, Predicate<PlayerAlliance> predicate) {
		for (PlayerAlliance alliance : getMembers()) {
			if (predicate.apply(alliance)) {
				alliance.sendPacket(packet, Predicates.<Player>alwaysTrue());
			}
		}
	}

	@Override
	public int onlineMembers() {
		return getMembers().size();
	}

	@Override
	public Race getRace() {
		return getLeaderObject().getRace();
	}

	public Player getCaptain() {
		return getLeaderObject().getLeaderObject();
	}

	@Override
	public boolean isFull() {
		return size() == 8;
	}

	public LootGroupRules getLootGroupRules() {
		return lootGroupRules;
	}

	public void setLootGroupRules(LootGroupRules lootGroupRules) {
		this.lootGroupRules = lootGroupRules;
	}

	/**
	 * @return sorted alliances by position
	 */
	public Collection<LeagueMember> getSortedMembers() {
		ArrayList<LeagueMember> newArrayList = Lists.newArrayList(members.values());
		Collections.sort(newArrayList, MEMBER_COMPARATOR);
		return newArrayList;
	}

	/**
	 * Reorganize alliances positions in league from 0 to size
	 *
	 * @return new league leader
	 */
	public Player reorganize() {
		int position = 0;
		Player newLeader = null;
		for (LeagueMember alliance : getSortedMembers()) {
			if (alliance.getLeaguePosition() > position) {
				if (position == 0) {
					newLeader = alliance.getObject().getLeaderObject();
					changeLeader(alliance);
				}
				alliance.setLeaguePosition(position);
			}
			position++;
		}
		return newLeader;
	}

	/**
	 * Search for player member in all alliances
	 *
	 * @return player object
	 */
	public Player getPlayerMember(Integer playerObjId) {
		for (PlayerAlliance member : getMembers()) {
			PlayerAllianceMember playerMember = member.getMember(playerObjId);
			if (playerMember != null) {
				return playerMember.getObject();
			}
		}
		return null;
	}

	public void broadcast() {
		broadcast(null, null);
	}

	public void broadcast(Player skippedPlayer) {
		broadcast(null, skippedPlayer);
	}

	public void broadcast(PlayerAlliance skippedAlliance) {
		broadcast(skippedAlliance, null);
	}

	public void broadcast(PlayerAlliance skippedAlliance, Player skippedPlayer) {
		lock();
		try {
			for (LeagueMember memberAlliance : members.values()) {
				PlayerAlliance targetAlliance = memberAlliance.getObject();
				if (!targetAlliance.equals(skippedAlliance)) {
					Predicate<Player> predicate = Predicates.<Player> alwaysTrue();
					if (skippedPlayer != null) {
						predicate = new PlayerFilters.ExcludePlayerFilter(skippedPlayer);
					}
					targetAlliance.sendPacket(new SM_ALLIANCE_INFO(targetAlliance, skippedAlliance), predicate);
					targetAlliance.sendPacket(new SM_SHOW_BRAND(0, 0, true), predicate);
				}
			}
		}
		finally {
			unlock();
		}
	}

	static class LeagueMemberComparator implements Comparator<LeagueMember> {

		@Override
		public int compare(LeagueMember o1, LeagueMember o2) {
			return o1.getLeaguePosition() > o2.getLeaguePosition() ? 1 : -1;
		}

	}

	public Collection<Player> getCaptains() {
		ArrayList<Player> newArrayList = Lists.newArrayList();
		for (LeagueMember member : getSortedMembers()) {
			Player leader = member.getObject().getLeaderObject();
			if (!newArrayList.contains(leader)) {
				newArrayList.add(leader);
			}
		}
		return newArrayList;
	}

}