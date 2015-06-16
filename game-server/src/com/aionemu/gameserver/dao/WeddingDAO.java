package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author synchro2
 */

public abstract class WeddingDAO implements DAO {

	@Override
	public final String getClassName() {
		return WeddingDAO.class.getName();
	}

	public abstract int loadPartnerId(Player player);

	public abstract void storeWedding(Player partner1, Player partner2);
	
	public abstract void deleteWedding(Player partner1, Player partner2);
}
