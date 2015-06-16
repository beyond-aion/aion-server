package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
public abstract class PlayerSettingsDAO implements DAO {

	/**
	 * Returns unique identifier for PlayerUiSettingsDAO
	 * 
	 * @return unique identifier for PlayerUiSettingsDAO
	 */
	@Override
	public final String getClassName() {
		return PlayerSettingsDAO.class.getName();
	}

	/**
	 * @param playerId
	 * @param data
	 */
	public abstract void saveSettings(final Player player);

	/**
	 * @param playerId
	 */
	public abstract void loadSettings(final Player player);
}
