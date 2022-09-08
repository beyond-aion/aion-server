package com.aionemu.gameserver.model.autogroup;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author xTz
 */
public class AGPlayer {

	private final int objectId;
	private Race race;
	private PlayerClass playerClass;
	private String name;
	private boolean isInInstance;
	private boolean isOnline;

	public AGPlayer(int objectId) {
			this.objectId = objectId;
	}

	public AGPlayer(Player player) {
		objectId = player.getObjectId();
		race = player.getRace();
		playerClass = player.getPlayerClass();
		name = player.getName();
		isOnline = true;
	}

	public int getObjectId() {
		return objectId;
	}

	public Race getRace() {
		return race;
	}

	public String getName() {
		return name;
	}

	public PlayerClass getPlayerClass() {
		return playerClass;
	}

	public void setInInstance(boolean result) {
		isInInstance = result;
	}

	public boolean isInInstance() {
		return isInInstance;
	}

	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean result) {
		isOnline = result;
	}
}
