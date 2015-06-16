package com.aionemu.gameserver.dao;

import java.util.Map;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.siege.SiegeLocation;

/**
 * @author Sarynth
 */
public abstract class SiegeDAO implements DAO {

	@Override
	public final String getClassName() {
		return SiegeDAO.class.getName();
	}

	public abstract boolean loadSiegeLocations(Map<Integer, SiegeLocation> locations);

	// private abstract boolean insertSiegeLocation(SiegeLocation siegeLocation);
	public abstract boolean updateSiegeLocation(SiegeLocation siegeLocation);

	public void updateLocation(final SiegeLocation siegeLocation) {
		updateSiegeLocation(siegeLocation);
	}

}