package com.aionemu.gameserver.model.autogroup;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author xTz
 */
public record AGPlayer(int objectId, Race race, PlayerClass playerClass, String name) {

	public AGPlayer(Player player) {
		this(player.getObjectId(), player.getRace(), player.getPlayerClass(), player.getName());
	}
}
