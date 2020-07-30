package com.aionemu.gameserver.dao;

import java.util.List;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.custom.instance.CustomInstanceRank;
import com.aionemu.gameserver.custom.instance.CustomInstanceRankedPlayer;
import com.aionemu.gameserver.model.Race;

/**
 * @author Jo
 */
public abstract class CustomInstanceDAO implements DAO {

	public abstract CustomInstanceRank loadPlayerRankObject(int playerId);

	public abstract boolean storePlayer(CustomInstanceRank rankObj);

	public abstract List<CustomInstanceRankedPlayer> loadTop10(Race race);

	@Override
	public String getClassName() {
		return CustomInstanceDAO.class.getName();
	}

}
