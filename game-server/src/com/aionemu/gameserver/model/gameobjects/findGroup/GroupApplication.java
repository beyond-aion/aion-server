package com.aionemu.gameserver.model.gameobjects.findGroup;

import com.aionemu.gameserver.model.gameobjects.player.Player;

public final class GroupApplication implements FindGroupEntry {

	private final Player player;
	private String message;
	private int groupType, classId, level;
	private int lastUpdate = (int) (System.currentTimeMillis() / 1000);

	public GroupApplication(Player player, String message, int groupType, int classId, int level) {
		this.player = player;
		this.message = message;
		this.groupType = groupType;
		this.classId = classId;
		this.level = level;
	}

	public Player getPlayer() {
		return player;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getGroupType() {
		return groupType;
	}

	public void setGroupType(int groupType) {
		this.groupType = groupType;
	}

	public int getClassId() {
		return classId;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getLastUpdate() {
		return lastUpdate;
	}

	public void updateLastUpdate() {
		lastUpdate = (int) (System.currentTimeMillis() / 1000);
	}
}
