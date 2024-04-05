package com.aionemu.gameserver.model.gameobjects.findGroup;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;

/**
 * Find Group
 * 
 * @author MrPoke
 */
public final class GroupRecruitment implements FindGroupEntry {

	private final AionObject object;
	private String message;
	private int groupType, classId = -1, level = -1;
	private int lastUpdate = (int) (System.currentTimeMillis() / 1000);

	public GroupRecruitment(AionObject object, String message, int groupType) {
		this.object = object;
		this.message = message;
		this.groupType = groupType;
	}

	public AionObject getObject() {
		return object;
	}

	public int getObjectId() {
		return object.getObjectId();
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
		if (classId != -1)
			return classId;
		if (object instanceof Player player)
			return player.getPlayerClass().getClassId();
		if (object instanceof TemporaryPlayerTeam<?> team)
			return team.getLeaderObject().getPlayerClass().getClassId();
		return 0;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	public int getMinLevel() {
		if (level != -1)
			return level;
		if (object instanceof Player player)
			return player.getLevel();
		if (object instanceof TemporaryPlayerTeam<?> team)
			return team.getMinExpPlayerLevel();
		return 1;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getMaxLevel() {
		if (object instanceof Player player)
			return player.getLevel();
		else if (object instanceof TemporaryPlayerTeam<?> team)
			return team.getMaxExpPlayerLevel();
		return 1;
	}

	public String getName() {
		return object instanceof TemporaryPlayerTeam<?> team ? team.getLeaderObject().getName(true) : ((Player) object).getName(true);
	}

	public int getSize() {
		return object instanceof TemporaryPlayerTeam<?> team ? team.size() : 1;
	}

	public int getLastUpdate() {
		return lastUpdate;
	}

	public void updateLastUpdate() {
		lastUpdate = (int) (System.currentTimeMillis() / 1000);
	}

	public Race getRace() {
		return object instanceof Player player ? player.getRace() : object instanceof TemporaryPlayerTeam<?> team ? team.getRace() : null;
	}
}
