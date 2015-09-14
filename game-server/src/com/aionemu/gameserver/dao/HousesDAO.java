package com.aionemu.gameserver.dao;

import java.util.Collection;
import java.util.Map;

import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HousingLand;

/**
 * @author Rolandas
 */
public abstract class HousesDAO implements IDFactoryAwareDAO {

	@Override
	public String getClassName() {
		return HousesDAO.class.getName();
	}

	@Override
	public abstract boolean supports(String databaseName, int majorVersion, int minorVersion);

	public abstract boolean isIdUsed(int houseObjectId);

	public abstract void storeHouse(House house);

	public abstract Map<Integer, House> loadHouses(Collection<HousingLand> lands, boolean studios);

	public abstract void deleteHouse(int playerId);
}
