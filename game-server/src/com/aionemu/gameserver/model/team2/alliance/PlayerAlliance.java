package com.aionemu.gameserver.model.team2.alliance;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.TeamType;
import com.aionemu.gameserver.model.team2.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.team2.league.League;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * @author ATracer
 */
public class PlayerAlliance extends TemporaryPlayerTeam<PlayerAllianceMember> {

	private final Map<Integer, PlayerAllianceGroup> groups = new HashMap<Integer, PlayerAllianceGroup>();
	private final Collection<Integer> viceCaptainIds = new CopyOnWriteArrayList<Integer>();
	private int allianceReadyStatus;
	private TeamType type;
	private League league;

	public PlayerAlliance(PlayerAllianceMember leader, TeamType type) {
		super(IDFactory.getInstance().nextId());
		this.type = type;
		initializeTeam(leader);
		for (int groupId = 1000; groupId <= 1003; groupId++) {
			groups.put(groupId, new PlayerAllianceGroup(this, groupId));
		}
	}

	@Override
	public void addMember(PlayerAllianceMember member) {
		super.addMember(member);
		PlayerAllianceGroup openAllianceGroup = getOpenAllianceGroup();
		openAllianceGroup.addMember(member);
	}

	@Override
	public void removeMember(PlayerAllianceMember member) {
		super.removeMember(member);
		member.getPlayerAllianceGroup().removeMember(member);
	}

	@Override
	public boolean isFull() {
		return size() == 24;
	}

	@Override
	public int getMinExpPlayerLevel() {
		int minLvl = 0;
		for (Player member : this.getMembers()) {
			if (member.getLevel() < minLvl) {
				minLvl = member.getLevel();
			}
		}
		return minLvl;
	}

	@Override
	public int getMaxExpPlayerLevel() {
		int maxLvl = 0;
		for (Player member : this.getMembers()) {
			if (member.getLevel() > maxLvl) {
				maxLvl = member.getLevel();
			}
		}
		return maxLvl;
	}

	public PlayerAllianceGroup getOpenAllianceGroup() {
		lock();
		try {
			for (int groupId = 1000; groupId <= 1003; groupId++) {
				PlayerAllianceGroup playerAllianceGroup = groups.get(groupId);
				if (!playerAllianceGroup.isFull()) {
					return playerAllianceGroup;
				}
			}
		} finally {
			unlock();
		}
		throw new IllegalStateException("All alliance groups are full.");
	}

	public PlayerAllianceGroup getAllianceGroup(Integer allianceGroupId) {
		PlayerAllianceGroup allianceGroup = groups.get(allianceGroupId);
		Objects.requireNonNull(allianceGroup, "No such alliance group " + allianceGroupId);
		return allianceGroup;
	}

	public final Collection<Integer> getViceCaptainIds() {
		return viceCaptainIds;
	}

	public final boolean isViceCaptain(Player player) {
		return viceCaptainIds.contains(player.getObjectId());
	}

	public final boolean isSomeCaptain(Player player) {
		return isLeader(player) || isViceCaptain(player);
	}

	public int getAllianceReadyStatus() {
		return allianceReadyStatus;
	}

	public void setAllianceReadyStatus(int allianceReadyStatus) {
		this.allianceReadyStatus = allianceReadyStatus;
	}

	public final League getLeague() {
		return league;
	}

	public final void setLeague(League league) {
		this.league = league;
	}

	public final boolean isInLeague() {
		return this.league != null;
	}

	public final int groupSize() {
		return groups.size();
	}

	public final Collection<PlayerAllianceGroup> getGroups() {
		return groups.values();
	}

	public TeamType getTeamType() {
		return type;
	}

}
