package com.aionemu.gameserver.dao;

import java.util.Map;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.custom.instance.CustomInstanceRank;

/**
 * @author Jo
 */
public abstract class CustomInstanceDAO implements DAO {

	public abstract Map<Integer, CustomInstanceRank> loadPlayerRanks();

	public abstract void storePlayer(int playerId);

	@Override
	public String getClassName() {
		return CustomInstanceDAO.class.getName();
	}

}
