package com.aionemu.gameserver.dao;

import java.util.List;
import java.util.Map;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.house.HouseBids;

/**
 * @author Rolandas
 */
public abstract class HouseBidsDAO implements DAO {

	@Override
	public final String getClassName() {
		return HouseBidsDAO.class.getName();
	}

	public abstract Map<Integer, HouseBids> loadBids();

	public abstract boolean addBid(HouseBids.Bid bid);

	public abstract boolean deleteOrDisableBids(int playerObjectId, List<HouseBids.Bid> bidsToDelete);

	public abstract boolean deleteHouseBids(int houseId);

}
