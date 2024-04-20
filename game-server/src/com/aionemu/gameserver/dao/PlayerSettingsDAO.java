package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerSettings;

/**
 * @author ATracer, Neon
 */
public abstract class PlayerSettingsDAO implements DAO {

	@Override
	public final String getClassName() {
		return PlayerSettingsDAO.class.getName();
	}

	public abstract void saveSettings(final Player player);

	public abstract PlayerSettings loadSettings(int playerId);
}
