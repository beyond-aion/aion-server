package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.ReadStH;
import com.aionemu.gameserver.model.broker.BrokerRace;
import com.aionemu.gameserver.model.gameobjects.BrokerItem;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;

public class BrokerDAO {

	private static final Logger log = LoggerFactory.getLogger(BrokerDAO.class);

	public static List<BrokerItem> loadBroker() {
		List<BrokerItem> brokerItems = new ArrayList<>();

		List<Item> items = InventoryDAO.loadBrokerItems();
		ItemStoneListDAO.load(items);

		DB.select("SELECT * FROM broker", new ReadStH() {

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					int itemPointer = rset.getInt("item_pointer");
					int itemId = rset.getInt("item_id");
					long itemCount = rset.getLong("item_count");
					String itemCreator = rset.getString("item_creator");
					int sellerId = rset.getInt("seller_id");
					long price = rset.getLong("price");
					BrokerRace itemBrokerRace = BrokerRace.valueOf(rset.getString("broker_race"));
					Timestamp expireTime = rset.getTimestamp("expire_time");
					Timestamp settleTime = rset.getTimestamp("settle_time");
					boolean isSold = rset.getBoolean("is_sold");
					boolean isSettled = rset.getBoolean("is_settled");
					boolean splittingAvailable = rset.getBoolean("splitting_available");

					Item item = null;
					if (!isSold)
						for (Item brItem : items) {
							if (itemPointer == brItem.getObjectId()) {
								item = brItem;
								// A partial purchase updates the listing count without saving a detached inventory item.
								if (item.getItemCount() != itemCount)
									item.setItemCount(itemCount);
								break;
							}
						}

					brokerItems.add(new BrokerItem(item, itemId, itemPointer, itemCount, itemCreator, price, sellerId, itemBrokerRace, isSold,
						isSettled, expireTime, settleTime, splittingAvailable));
				}
			}
		});

		return brokerItems;
	}

	public static boolean store(BrokerItem item) {
		if (item == null) {
			log.warn("Null broker item on save");
			return false;
		}

		boolean result;
		try (Connection con = DatabaseFactory.getConnection()) {
			result = write(con, item);
		} catch (SQLException e) {
			log.error("Could not save broker listing {}", item.getItemUniqueId(), e);
			return false;
		}

		if (result)
			item.setPersistentState(PersistentState.UPDATED);

		return result;
	}

	public static boolean register(BrokerItem listing, List<Item> items) {
		if (!InventoryDAO.storeWithBrokerItems(items, listing.getSellerId(), List.of(listing)))
			return false;
		listing.setPersistentState(PersistentState.UPDATED);
		return true;
	}

	/**
	 * Saves the buyer's items and the listings of a purchase in one transaction. The caller marks the listings as saved.
	 */
	public static boolean storePurchase(List<BrokerItem> listings, List<Item> items, int buyerId) {
		return InventoryDAO.storeWithBrokerItems(items, buyerId, listings);
	}

	/**
	 * @return False if the state of the listing needs no write.
	 */
	static boolean write(Connection con, BrokerItem item) throws SQLException {
		switch (item.getPersistentState()) {
			case NEW:
				insertBrokerItem(con, item);
				return true;
			case DELETED:
				deleteBrokerItem(con, item);
				return true;
			case UPDATE_REQUIRED:
				updateBrokerItem(con, item);
				return true;
			default:
				return false;
		}
	}

	private static void insertBrokerItem(Connection con, BrokerItem item) throws SQLException {
		try (PreparedStatement stmt = con.prepareStatement(
			"INSERT INTO `broker` (`item_pointer`, `item_id`, `item_count`, `item_creator`, `price`, `broker_race`, `expire_time`, `seller_id`, `is_sold`, `is_settled`, `splitting_available`) VALUES (?,?,?,?,?,?,?,?,?,?,?)")) {
			stmt.setInt(1, item.getItemUniqueId());
			stmt.setInt(2, item.getItemId());
			stmt.setLong(3, item.getItemCount());
			stmt.setString(4, item.getItemCreator());
			stmt.setLong(5, item.getPrice());
			stmt.setString(6, String.valueOf(item.getItemBrokerRace()));
			stmt.setTimestamp(7, item.getExpireTime());
			stmt.setInt(8, item.getSellerId());
			stmt.setBoolean(9, item.isSold());
			stmt.setBoolean(10, item.isSettled());
			stmt.setBoolean(11, item.isSplittingAvailable());
			stmt.executeUpdate();
		}
	}

	private static void deleteBrokerItem(Connection con, BrokerItem item) throws SQLException {
		try (PreparedStatement stmt = con.prepareStatement("DELETE FROM `broker` WHERE `item_pointer` = ? AND `seller_id` = ? AND `expire_time` = ?")) {
			stmt.setInt(1, item.getItemUniqueId());
			stmt.setInt(2, item.getSellerId());
			stmt.setTimestamp(3, item.getExpireTime());
			stmt.execute();
		}
	}

	private static void updateBrokerItem(Connection con, BrokerItem item) throws SQLException {
		try (PreparedStatement stmt = con.prepareStatement("UPDATE broker SET `is_sold` = ?, `is_settled` = ?, `settle_time` = ?, `item_count` = ?"
			+ " WHERE `item_pointer` = ? AND `expire_time` = ? AND `seller_id` = ? AND `is_settled` = 0")) {
			stmt.setBoolean(1, item.isSold());
			stmt.setBoolean(2, item.isSettled());
			stmt.setTimestamp(3, item.getSettleTime());
			stmt.setLong(4, item.getItemCount());
			stmt.setInt(5, item.getItemUniqueId());
			stmt.setTimestamp(6, item.getExpireTime());
			stmt.setInt(7, item.getSellerId());
			stmt.execute();
		}
	}

}
