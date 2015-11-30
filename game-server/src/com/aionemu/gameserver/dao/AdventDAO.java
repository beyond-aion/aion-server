package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Nathan
 */
public abstract class AdventDAO implements DAO {

	@Override
	public String getClassName() {
		return AdventDAO.class.getName();
	}

	public abstract boolean containAllready(Player player);

	public abstract int get(Player player);

	public abstract void set(Player player, int date);

	public abstract boolean newAdvent(Player player);
}
