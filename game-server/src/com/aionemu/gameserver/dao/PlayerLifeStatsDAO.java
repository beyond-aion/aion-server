package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Mr. Poke
 */
public abstract class PlayerLifeStatsDAO implements DAO {

	/**
	 * Returns unique identifier for PlayerLifeStatsDAO
	 * 
	 * @return unique identifier for PlayerLifeStatsDAO
	 */
	@Override
	public final String getClassName() {
		return PlayerLifeStatsDAO.class.getName();
	}

	/**
	 * @param player
	 */
	public abstract void loadPlayerLifeStat(Player player);

	/**
	 * @param player
	 */
	public abstract void insertPlayerLifeStat(Player player);

	/**
	 * @param player
	 */
	public abstract void updatePlayerLifeStat(Player player);
}
