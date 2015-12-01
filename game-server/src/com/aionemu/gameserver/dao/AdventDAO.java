package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Neon
 */
public abstract class AdventDAO implements DAO {

	@Override
	public String getClassName() {
		return AdventDAO.class.getName();
	}

	public abstract int getLastReceivedDay(Player player);

	public abstract boolean storeLastReceivedDay(Player player, int dayOfMonth);
}
