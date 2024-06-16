package com.aionemu.gameserver.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ReadStH;
import com.aionemu.gameserver.model.broker.BrokerRace;
import com.aionemu.gameserver.model.gameobjects.BrokerItem;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;

public class BrokerDAO {

	private static final Logger log = LoggerFactory.getLogger(BrokerDAO.class);

	public static List<BrokerItem> loadBroker() {
		List<BrokerItem> brokerItems = new ArrayList<>();

		List<Item> items = getBrokerItems();

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
					int sold = rset.getInt("is_sold");
					int settled = rset.getInt("is_settled");
					boolean splittingAvailable = rset.getInt("splitting_available") == 1 ? true : false;

					boolean isSold = sold == 1;
					boolean isSettled = settled == 1;

					Item item = null;
					if (!isSold)
						for (Item brItem : items) {
							if (itemPointer == brItem.getObjectId()) {
								item = brItem;
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

	private static List<Item> getBrokerItems() {
		List<Item> brokerItems = new ArrayList<>();

		DB.select("SELECT * FROM inventory WHERE `item_location` = 126", new ReadStH() {

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					int itemUniqueId = rset.getInt("item_unique_id");
					int itemId = rset.getInt("item_id");
					long itemCount = rset.getLong("item_count");
					Integer itemColor = (Integer) rset.getObject("item_color");
					int colorExpireTime = rset.getInt("color_expires");
					String itemCreator = rset.getString("item_creator");
					int expireTime = rset.getInt("expire_time");
					int activationCount = rset.getInt("activation_count");
					long slot = rset.getLong("slot");
					int location = rset.getInt("item_location");
					int enchant = rset.getInt("enchant");
					int enchantBonus = rset.getInt("enchant_bonus");
					int itemSkin = rset.getInt("item_skin");
					int fusionedItem = rset.getInt("fusioned_item");
					int optionalSocket = rset.getInt("optional_socket");
					int optionalFusionSocket = rset.getInt("optional_fusion_socket");
					int charge = rset.getInt("charge");
					int tuneCount = rset.getInt("tune_count");
					int bonusStatsId = rset.getInt("rnd_bonus");
					int fusionedItemBonusStatsId = rset.getInt("fusion_rnd_bonus");
					int tempering = rset.getInt("tempering");
					int packCount = rset.getInt("pack_count");
					int isAmplified = rset.getInt("is_amplified");
					int buffSkill = rset.getInt("buff_skill");
					int rndPlumeBonusValue = rset.getInt("rnd_plume_bonus");

					brokerItems.add(new Item(itemUniqueId, itemId, itemCount, itemColor, colorExpireTime, itemCreator, expireTime, activationCount, false,
						false, slot, location, enchant, enchantBonus, itemSkin, fusionedItem, optionalSocket, optionalFusionSocket, charge, tuneCount,
						bonusStatsId, fusionedItemBonusStatsId, tempering, packCount, isAmplified == 1, buffSkill, rndPlumeBonusValue));
				}
			}
		});

		return brokerItems;
	}

	public static boolean store(BrokerItem item) {
		boolean result = false;

		if (item == null) {
			log.warn("Null broker item on save");
			return result;
		}

		switch (item.getPersistentState()) {
			case NEW:
				result = insertBrokerItem(item);
				if (item.getItem() != null)
					InventoryDAO.store(item.getItem(), item.getSellerId());
				break;

			case DELETED:
				result = deleteBrokerItem(item);
				break;

			case UPDATE_REQUIRED:
				result = updateBrokerItem(item);
				break;
		}

		if (result)
			item.setPersistentState(PersistentState.UPDATED);

		return result;
	}

	private static boolean insertBrokerItem(BrokerItem item) {
		boolean result = DB.insertUpdate(
			"INSERT INTO `broker` (`item_pointer`, `item_id`, `item_count`, `item_creator`, `price`, `broker_race`, `expire_time`, `seller_id`, `is_sold`, `is_settled`, `splitting_available`) VALUES (?,?,?,?,?,?,?,?,?,?,?)",
			new IUStH() {

				@Override
				public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
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
					stmt.execute();
				}
			});

		return result;
	}

	private static boolean deleteBrokerItem(BrokerItem item) {
		boolean result = DB.insertUpdate("DELETE FROM `broker` WHERE `item_pointer` = ? AND `seller_id` = ? AND `expire_time` = ?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, item.getItemUniqueId());
				stmt.setInt(2, item.getSellerId());
				stmt.setTimestamp(3, item.getExpireTime());
				stmt.execute();
			}
		});

		return result;
	}

	private static boolean updateBrokerItem(BrokerItem item) {
		boolean result = DB.insertUpdate(
			"UPDATE broker SET `is_sold` = ?, `is_settled` = ?, `settle_time` = ?, `item_count` = ? WHERE `item_pointer` = ? AND `expire_time` = ? AND `seller_id` = ? AND `is_settled` = 0",
			new IUStH() {

				@Override
				public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
					stmt.setBoolean(1, item.isSold());
					stmt.setBoolean(2, item.isSettled());
					stmt.setTimestamp(3, item.getSettleTime());
					stmt.setLong(4, item.getItemCount());
					stmt.setInt(5, item.getItemUniqueId());
					stmt.setTimestamp(6, item.getExpireTime());
					stmt.setInt(7, item.getSellerId());
					stmt.execute();
				}
			});

		return result;
	}

}
