package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;

/**
 * This interface is generic one for all DAO classes that are generating their id's using {@link com.aionemu.gameserver.utils.idfactory.IDFactory}
 *
 * @author SoulKeeper
 */
public interface IDFactoryAwareDAO {

	/**
	 * @return All IDs that are used by this DAO or null on fail
	 */
	int[] getUsedIDs();
}
