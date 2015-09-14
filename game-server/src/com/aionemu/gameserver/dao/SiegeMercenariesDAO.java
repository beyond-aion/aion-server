package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.Race;

/**
 * @author ViAl
 */
public abstract class SiegeMercenariesDAO implements DAO {

	public abstract void loadActiveMercenaries();

	public abstract void deleteMercenaries(int locationId, int zoneId);

	public abstract void insertMercenaries(int locationId, int zoneId, Race race);

	@Override
	public String getClassName() {
		return SiegeMercenariesDAO.class.getName();
	}

}
