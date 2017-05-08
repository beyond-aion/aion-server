package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;

/**
 * Find Group
 * 
 * @author MrPoke
 */
public class FindGroup {

	private AionObject object;
	private String message;
	private int groupType, minMembers, instanceId;
	private int lastUpdate = (int) (System.currentTimeMillis() / 1000);

	public FindGroup(AionObject object, String message, int groupType, int instanceId, int minMembers) {
		this(object, message, groupType);
		this.instanceId = instanceId;
		this.minMembers = minMembers;
	}

	public FindGroup(AionObject object, String message, int groupType) {
		this.object = object;
		this.message = message;
		this.groupType = groupType;
	}

	public String getMessage() {
		return message;
	}

	public int getGroupType() {
		return groupType;
	}

	public int getObjectId() {
		return object.getObjectId();
	}

	public AionObject getObject() {
		return object;
	}

	public int getInstanceId() {
		return instanceId;
	}

	public int getMinMembers() {
		return minMembers;
	}

	public int getClassId() {
		if (object instanceof Player)
			return ((Player) (object)).getPlayerClass().getClassId();
		else if (object instanceof TemporaryPlayerTeam)
			return ((TemporaryPlayerTeam<?>) object).getLeaderObject().getPlayerClass().getClassId();
		return 0;
	}

	public int getMinLevel() {
		if (object instanceof Player)
			return ((Player) (object)).getLevel();
		else if (object instanceof TemporaryPlayerTeam)
			return ((TemporaryPlayerTeam<?>) object).getMinExpPlayerLevel();
		return 1;
	}

	public int getMaxLevel() {
		if (object instanceof Player)
			return ((Player) (object)).getLevel();
		else if (object instanceof TemporaryPlayerTeam)
			return ((TemporaryPlayerTeam<?>) object).getMaxExpPlayerLevel();
		return 1;
	}

	/**
	 * @return the lastUpdate
	 */
	public int getLastUpdate() {
		return lastUpdate;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		if (object instanceof TemporaryPlayerTeam)
			return ((TemporaryPlayerTeam<?>) object).getLeaderObject().getName();
		return object.getName();
	}

	public int getSize() {
		if (object instanceof TemporaryPlayerTeam)
			return ((TemporaryPlayerTeam<?>) object).size();
		return 1;
	}

	public void setMessage(String message) {
		lastUpdate = (int) (System.currentTimeMillis() / 1000);
		this.message = message;
	}
}
