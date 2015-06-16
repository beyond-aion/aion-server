package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author synchro2
 */
public abstract class CraftCooldownsDAO implements DAO {

	@Override
	public final String getClassName() {
		return CraftCooldownsDAO.class.getName();
	}

	public abstract void loadCraftCooldowns(Player player);

	public abstract void storeCraftCooldowns(Player player);

}