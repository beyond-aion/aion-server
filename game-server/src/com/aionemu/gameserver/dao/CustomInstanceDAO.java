package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.custom.instance.CustomInstanceRank;

/**
 * @author Jo
 */
public abstract class CustomInstanceDAO implements DAO {

	public abstract CustomInstanceRank loadPlayerRankObject(int playerId);

	public abstract boolean storePlayer(CustomInstanceRank rankObj);

	public abstract void deletePlayer(int playerId);

	@Override
	public String getClassName() {
		return CustomInstanceDAO.class.getName();
	}

}
