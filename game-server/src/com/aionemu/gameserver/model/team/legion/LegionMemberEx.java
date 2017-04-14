package com.aionemu.gameserver.model.team.legion;

import java.sql.Timestamp;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Simple
 */
public class LegionMemberEx extends LegionMember {

	private String name;
	private PlayerClass playerClass;
	private int level;
	private int lastOnlineEpochSeconds;
	private int worldId;
	private boolean online = false;

	/**
	 * If player is immediately after this constructor is called
	 */
	public LegionMemberEx(Player player, LegionMember legionMember, boolean online) {
		super(player.getObjectId(), legionMember.getLegion(), legionMember.getRank());
		this.nickname = legionMember.getNickname();
		this.selfIntro = legionMember.getSelfIntro();
		this.name = player.getName();
		this.playerClass = player.getPlayerClass();
		this.level = player.getLevel();
		this.lastOnlineEpochSeconds = player.getCommonData().getLastOnlineEpochSeconds();
		this.worldId = player.getPosition().getMapId();
		this.online = online;
	}

	/**
	 * If player is defined later on this constructor is called
	 */
	public LegionMemberEx(int playerObjId) {
		super(playerObjId);
	}

	/**
	 * If player is defined later on this constructor is called
	 */
	public LegionMemberEx(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public PlayerClass getPlayerClass() {
		return playerClass;
	}

	public void setPlayerClass(PlayerClass playerClass) {
		this.playerClass = playerClass;
	}

	public int getLastOnlineEpochSeconds() {
		return lastOnlineEpochSeconds;
	}

	public void setLastOnline(Timestamp timestamp) {
		lastOnlineEpochSeconds = timestamp == null ? 0 : (int) (timestamp.getTime() / 1000);
	}

	public int getLevel() {
		return level;
	}

	public void setLevelByExp(long exp) {
		this.level = playerClass.isStartingClass() ? 9 : DataManager.PLAYER_EXPERIENCE_TABLE.getLevelForExp(exp);
	}

	public int getWorldId() {
		return worldId;
	}

	public void setWorldId(int worldId) {
		this.worldId = worldId;
	}

	/**
	 * @param online
	 *          the online to set
	 */
	public void setOnline(boolean online) {
		this.online = online;
	}

	/**
	 * @return the online
	 */
	public boolean isOnline() {
		return online;
	}

}
