package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Neon
 */
public abstract class VeteranRewardDAO implements DAO {

	public abstract int loadReceivedMonths(Player player);

	public abstract boolean storeReceivedMonths(Player player, int months);

	@Override
	public String getClassName() {
		return VeteranRewardDAO.class.getName();
	}
}
