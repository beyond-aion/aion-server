package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author nrg
 */
public abstract class PlayerCooldownsDAO implements DAO {

	/**
	 * Returns unique identifier for PlayerCooldownsDAO
	 * 
	 * @return unique identifier for PlayerCooldownsDAO
	 */
	@Override
	public final String getClassName() {
		return PlayerCooldownsDAO.class.getName();
	}

	/**
	 * @param player
	 */
	public abstract void loadPlayerCooldowns(Player player);

	/**
	 * @param player
	 */
	public abstract void storePlayerCooldowns(Player player);

}
