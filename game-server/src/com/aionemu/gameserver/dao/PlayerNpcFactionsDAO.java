package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author MrPoke
 */
public abstract class PlayerNpcFactionsDAO implements DAO {

	@Override
	public String getClassName() {
		return PlayerNpcFactionsDAO.class.getName();
	}

	public abstract void loadNpcFactions(Player player);

	public abstract void storeNpcFactions(Player player);

}
