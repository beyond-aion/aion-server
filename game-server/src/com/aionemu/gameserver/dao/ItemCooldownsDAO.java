package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author ATracer
 */
public abstract class ItemCooldownsDAO implements DAO {

	/**
	 * Returns unique identifier for ItemCooldownsDAO
	 * 
	 * @return unique identifier for ItemCooldownsDAO
	 */
	@Override
	public final String getClassName() {
		return ItemCooldownsDAO.class.getName();
	}

	/**
	 * @param player
	 */
	public abstract void loadItemCooldowns(Player player);

	/**
	 * @param player
	 */
	public abstract void storeItemCooldowns(Player player);

}
