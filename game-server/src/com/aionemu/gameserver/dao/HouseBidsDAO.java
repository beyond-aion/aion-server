package com.aionemu.gameserver.dao;

import java.sql.Timestamp;
import java.util.Set;

import com.aionemu.commons.database.dao.DAO;
import com.aionemu.gameserver.model.house.PlayerHouseBid;

/**
 * @author Rolandas
 */
public abstract class HouseBidsDAO implements DAO {

	@Override
	public final String getClassName() {
		return HouseBidsDAO.class.getName();
	}

	public abstract Set<PlayerHouseBid> loadBids();

	public abstract boolean addBid(int playerId, int houseId, long bidOffer, Timestamp time);

	public abstract void changeBid(int playerId, int houseId, long newBidOffer, Timestamp time);

	public abstract void deleteHouseBids(int houseId);

}
