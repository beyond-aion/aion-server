package com.aionemu.gameserver.model.team.league;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.GeneralTeam;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceMember;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.network.aion.AionServerPacket;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.utils.collections.Predicates;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * @author ATracer
 */
public class League extends GeneralTeam<PlayerAlliance, LeagueMember> {

	private LootGroupRules lootGroupRules = new LootGroupRules();

	public League(LeagueMember leader) {
		super(IDFactory.getInstance().nextId(), true);
		setLeader(leader);
	}

	@Override
	public List<Player> getOnlineMembers() {
		return getMembers().stream().flatMap(alliance -> alliance.getOnlineMembers().stream()).collect(Collectors.toList());
	}

	@Override
	public void addMember(LeagueMember member) {
		super.addMember(member);
		member.getObject().setLeague(this);
	}

	@Override
	public void onRemoveMember(LeagueMember member) {
		member.getObject().setLeague(null);
	}

	@Override
	public int getMaxMemberCount() {
		return 8;
	}

	@Override
	public void sendPackets(AionServerPacket... packets) {
		for (PlayerAlliance alliance : getMembers()) {
			alliance.sendPackets(packets);
		}
	}

	@Override
	public void sendPacket(Predicate<PlayerAlliance> predicate, AionServerPacket... packets) {
		for (PlayerAlliance alliance : getMembers()) {
			if (predicate.test(alliance))
				alliance.sendPackets(packets);
		}
	}

	@Override
	public Race getRace() {
		return getLeaderObject().getRace();
	}

	public Player getCaptain() {
		return getLeaderObject().getLeaderObject();
	}

	@Override
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
		List<LeagueMember> memberList = new ArrayList<>(members.values());
		memberList.sort(Comparator.comparing(LeagueMember::getLeaguePosition));
		return memberList;
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
	public Player getPlayerMember(int playerObjId) {
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
					Predicate<Player> predicate = Predicates.alwaysTrue();
					if (skippedPlayer != null) {
						predicate = Predicates.Players.allExcept(skippedPlayer);
					}
					targetAlliance.sendPacket(predicate, new SM_ALLIANCE_INFO(targetAlliance, skippedAlliance));
				}
			}
		} finally {
			unlock();
		}
	}

	public Collection<Player> getCaptains() {
		List<Player> captains = new ArrayList<>();
		for (LeagueMember member : getSortedMembers()) {
			Player leader = member.getObject().getLeaderObject();
			if (!captains.contains(leader)) {
				captains.add(leader);
			}
		}
		return captains;
	}
}
