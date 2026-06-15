package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.house.HouseBids;

/**
 * @author Rolandas
 */
public class HouseBidsDAO {

	private static final Logger log = LoggerFactory.getLogger(HouseBidsDAO.class);

	public static final String LOAD_QUERY = "SELECT b.*, IF(b.player_id = p.id, 1, 0) playerExists FROM `house_bids` b LEFT JOIN `players` p ON p.id = b.player_id ORDER BY `bid`, `bid_time`";
	public static final String INSERT_QUERY = "INSERT INTO `house_bids` (`player_id`, `house_id`, `bid`, `bid_time`) VALUES (?, ?, ?, ?)";
	public static final String DELETE_QUERY = "DELETE FROM `house_bids` WHERE `house_id` = ?";
	public static final String DELETE_SINGLE_BID_QUERY = "DELETE FROM `house_bids` WHERE `player_id` = ? AND `house_id` = ? AND `bid` = ?";
	public static final String DISABLE_QUERY = "UPDATE `house_bids` SET `player_id` = 0 WHERE `player_id` = ?";

	public static Set<Integer> loadBids(Map<Integer, HouseBids> bidsById) {
		Set<Integer> deletedPlayerIds = new HashSet<>();
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(LOAD_QUERY)) {
			try (ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					int playerId = rset.getInt("player_id");
					int houseId = rset.getInt("house_id");
					long bidOffer = rset.getLong("bid");
					Timestamp time = rset.getTimestamp("bid_time");
					HouseBids houseBids = bidsById.get(houseId);
					if (houseBids == null)
						bidsById.put(houseId, new HouseBids(houseId, bidOffer, time.getTime()));
					else {
						houseBids.bid(playerId, bidOffer, time.getTime());
						if (playerId != 0 && !rset.getBoolean("playerExists"))
							deletedPlayerIds.add(playerId);
					}
				}
			}
		} catch (Exception e) {
			log.error("Cannot read house bids", e);
		}
		return deletedPlayerIds;
	}

	public static boolean addBid(HouseBids.Bid bid) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
			stmt.setInt(1, bid.getPlayerObjectId());
			stmt.setInt(2, bid.getHouseObjectId());
			stmt.setLong(3, bid.getKinah());
			stmt.setTimestamp(4, new Timestamp(bid.getTime()));
			stmt.execute();
		} catch (Exception e) {
			log.error("Cannot insert house bid", e);
			return false;
		}
		return true;
	}

	public static boolean deleteOrDisableBids(int playerObjectId, List<HouseBids.Bid> bidsToDelete) {
		try (Connection con = DatabaseFactory.getConnection()) {
			if (!bidsToDelete.isEmpty()) {
				try (PreparedStatement delStmt = con.prepareStatement(DELETE_SINGLE_BID_QUERY)) {
					for (HouseBids.Bid bid : bidsToDelete) {
						delStmt.setInt(1, bid.getPlayerObjectId());
						delStmt.setInt(2, bid.getHouseObjectId());
						delStmt.setLong(3, bid.getKinah());
						delStmt.execute();
					}
				}
			}
			try (PreparedStatement stmt = con.prepareStatement(DISABLE_QUERY)) {
				stmt.setInt(1, playerObjectId);
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			log.error("Cannot delete or disable house bids for player " + playerObjectId, e);
			return false;
		}
		return true;
	}

	public static boolean deleteHouseBids(int houseId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DELETE_QUERY)) {
			stmt.setInt(1, houseId);
			stmt.execute();
			return true;
		} catch (Exception e) {
			log.error("Cannot delete house bids", e);
			return false;
		}
	}

}
