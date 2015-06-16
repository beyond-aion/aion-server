package com.aionemu.gameserver.dao;

import com.aionemu.gameserver.model.house.HouseRegistry;

/**
 * @author Rolandas
 */
public abstract class PlayerRegisteredItemsDAO implements IDFactoryAwareDAO {

	@Override
	public String getClassName() {
		return PlayerRegisteredItemsDAO.class.getName();
	}
	
	public abstract void loadRegistry(int playerId);

	public abstract boolean store(HouseRegistry registry, int playerId);

	public abstract boolean deletePlayerItems(int playerId);
	
	public abstract void resetRegistry(int playerId);

}
