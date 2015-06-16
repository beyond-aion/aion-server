package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

public abstract class PortalCooldownsDAO implements DAO {

	/**
	 * Returns unique identifier for PortalCooldownsDAO
	 * 
	 * @return unique identifier for PortalCooldownsDAO
	 */
	@Override
	public final String getClassName() {
		return PortalCooldownsDAO.class.getName();
	}

	/**
	 * @param player
	 */
	public abstract void loadPortalCooldowns(Player player);

	/**
	 * @param player
	 */
	public abstract void storePortalCooldowns(Player player);

}
