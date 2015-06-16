package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Rolandas
 */
public abstract class HouseObjectCooldownsDAO implements DAO {

	@Override
	public String getClassName() {
		return HouseObjectCooldownsDAO.class.getName();
	}

	public abstract void loadHouseObjectCooldowns(Player player);

	public abstract void storeHouseObjectCooldowns(Player player);

}
