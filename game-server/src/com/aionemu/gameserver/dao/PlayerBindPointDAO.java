package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author evilset
 */
public abstract class PlayerBindPointDAO implements DAO {

	@Override
	public String getClassName() {
		return PlayerBindPointDAO.class.getName();
	}

	public abstract void loadBindPoint(Player player);

	public abstract boolean insertBindPoint(Player player);

	public abstract boolean updateBindPoint(Player player);

	public abstract boolean store(Player player);

}
