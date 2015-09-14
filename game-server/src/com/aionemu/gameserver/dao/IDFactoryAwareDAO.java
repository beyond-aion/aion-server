package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;

/**
 * This interface is generic one for all DAO classes that are generating their id's using {@link com.aionemu.gameserver.utils.idfactory.IDFactory}
 * 
 * @author SoulKeeper
 */
public interface IDFactoryAwareDAO extends DAO {

	/**
	 * Returns array of all id's that are used by this DAO
	 * 
	 * @return array of used id's
	 */
	public int[] getUsedIDs();
}
