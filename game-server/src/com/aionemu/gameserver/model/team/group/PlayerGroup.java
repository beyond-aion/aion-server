package com.aionemu.gameserver.model.team.group;

import com.aionemu.gameserver.model.team.TeamType;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * @author ATracer
 */
public class PlayerGroup extends TemporaryPlayerTeam<PlayerGroupMember> {

	private final PlayerGroupStats playerGroupStats;
	private TeamType type;

	public PlayerGroup(PlayerGroupMember leader, TeamType type, int id) {
		super(id == 0 ? IDFactory.getInstance().nextId() : id, id == 0);
		this.playerGroupStats = new PlayerGroupStats(this);
		this.type = type;
		setLeader(leader);
	}

	@Override
	public void addMember(PlayerGroupMember member) {
		super.addMember(member);
		playerGroupStats.onAddPlayer(member);
		member.getObject().setPlayerGroup(this);
	}

	@Override
	public void onRemoveMember(PlayerGroupMember member) {
		playerGroupStats.onRemovePlayer(member);
		member.getObject().setPlayerGroup(null);
	}

	@Override
	public int getMaxMemberCount() {
		return 6;
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
