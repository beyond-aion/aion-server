package com.aionemu.gameserver.model.gameobjects.findGroup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;

public final class ServerWideGroup implements FindGroupEntry {

	private final List<Player> members = new ArrayList<>();
	private final int instanceMaskId;
	private final int minMembers;
	private String message;
	private int lastUpdate;

	public ServerWideGroup(Player recruiter, int instanceMaskId, int minMembers, String message) {
		this.members.add(recruiter);
		this.instanceMaskId = instanceMaskId;
		this.minMembers = minMembers;
		this.message = message;
		setLastUpdate();
	}

	public List<Player> getMembers() {
		// custom: use regular teams for server-wide instance group recruitment (on official servers the recruiter must not be in a team)
		TemporaryPlayerTeam<?> team = getRecruiter().getCurrentTeam();
		return team == null ? members : team.getMembers();
	}

	public int getInstanceMaskId() {
		return instanceMaskId;
	}

	public int getMinMembers() {
		return minMembers;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate() {
		this.lastUpdate = (int) (System.currentTimeMillis() / 1000);
	}

	public Player getRecruiter() {
		return members.get(0);
	}

	public int getId() {
		return getRecruiter().getObjectId();
	}

	public Race getRace() {
		return getRecruiter().getRace();
	}

	public int getMinLevel() {
		return members.stream().max(Comparator.comparing(Player::getLevel)).map(Player::getLevel).get();
	}

	public int getMaxLevel() {
		return members.stream().max(Comparator.comparing(Player::getLevel).reversed()).map(Player::getLevel).get();
	}
}
