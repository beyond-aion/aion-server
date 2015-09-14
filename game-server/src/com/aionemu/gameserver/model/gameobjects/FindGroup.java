package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.group.PlayerGroup;

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
		else if (object instanceof PlayerAlliance)
			((PlayerAlliance) (object)).getLeaderObject().getCommonData().getPlayerClass();
		else if (object instanceof PlayerGroup) {
			((PlayerGroup) object).getLeaderObject().getPlayerClass();
		}
		return 0;
	}

	public int getMinLevel() {
		if (object instanceof Player)
			return ((Player) (object)).getLevel();
		else if (object instanceof PlayerAlliance) {
			int minLvl = 99;
			for (Player member : ((PlayerAlliance) (object)).getMembers()) {
				int memberLvl = member.getCommonData().getLevel();
				if (memberLvl < minLvl)
					minLvl = memberLvl;
			}
			return minLvl;
		} else if (object instanceof PlayerGroup) {
			return ((PlayerGroup) object).getMinExpPlayerLevel();
		} else if (object instanceof TemporaryPlayerTeam) {
			return ((TemporaryPlayerTeam<?>) object).getMinExpPlayerLevel();
		}
		return 1;
	}

	public int getMaxLevel() {
		if (object instanceof Player)
			return ((Player) (object)).getLevel();
		else if (object instanceof PlayerAlliance) {
			int maxLvl = 1;
			for (Player member : ((PlayerAlliance) (object)).getMembers()) {
				int memberLvl = member.getCommonData().getLevel();
				if (memberLvl > maxLvl)
					maxLvl = memberLvl;
			}
			return maxLvl;
		} else if (object instanceof PlayerGroup) {
			return ((PlayerGroup) object).getMaxExpPlayerLevel();
		} else if (object instanceof TemporaryPlayerTeam) {
			return ((TemporaryPlayerTeam<?>) object).getMaxExpPlayerLevel();
		}
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
		if (object instanceof Player)
			return ((Player) object).getName();
		else if (object instanceof PlayerAlliance)
			return ((PlayerAlliance) object).getLeaderObject().getCommonData().getName();
		else if (object instanceof PlayerGroup) {
			return ((PlayerGroup) object).getLeaderObject().getName();
		}
		return "";
	}

	public int getSize() {
		if (object instanceof Player)
			return 1;
		else if (object instanceof PlayerAlliance)
			return ((PlayerAlliance) object).size();
		else if (object instanceof PlayerGroup) {
			return ((PlayerGroup) object).size();
		}
		return 1;
	}

	public void setMessage(String message) {
		lastUpdate = (int) (System.currentTimeMillis() / 1000);
		this.message = message;
	}
}
