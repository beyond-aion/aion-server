package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
public abstract class PlayerEffectsDAO implements DAO {

	/**
	 * Returns unique identifier for PlayerEffectsDAO
	 * 
	 * @return unique identifier for PlayerEffectsDAO
	 */
	@Override
	public final String getClassName() {
		return PlayerEffectsDAO.class.getName();
	}

	/**
	 * @param player
	 */
	public abstract void loadPlayerEffects(Player player);

	/**
	 * @param player
	 */
	public abstract void storePlayerEffects(Player player);

}
