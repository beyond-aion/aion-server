package com.aionemu.gameserver.model.team2.group;

import com.aionemu.gameserver.model.team2.TeamType;
import com.aionemu.gameserver.model.team2.TemporaryPlayerTeam;

/**
 * @author ATracer
 */
public class PlayerGroup extends TemporaryPlayerTeam<PlayerGroupMember> {

	private final PlayerGroupStats playerGroupStats;
	private TeamType type;

	public PlayerGroup(PlayerGroupMember leader, TeamType type, int id) {
		super(id);
		this.playerGroupStats = new PlayerGroupStats(this);
		this.type = type;
		initializeTeam(leader);
	}

	@Override
	public void addMember(PlayerGroupMember member) {
		super.addMember(member);
		playerGroupStats.onAddPlayer(member);
		member.getObject().setPlayerGroup2(this);
	}

	@Override
	public void removeMember(PlayerGroupMember member) {
		super.removeMember(member);
		playerGroupStats.onRemovePlayer(member);
		member.getObject().setPlayerGroup2(null);
	}

	@Override
	public boolean isFull() {
		return size() == 6;
	}

	@Override
	public int getMinExpPlayerLevel() {
		return playerGroupStats.getMinExpPlayerLevel();
	}

	@Override
	public int getMaxExpPlayerLevel() {
		return playerGroupStats.getMaxExpPlayerLevel();
	}

	public TeamType getTeamType() {
		return type;
	}

}
