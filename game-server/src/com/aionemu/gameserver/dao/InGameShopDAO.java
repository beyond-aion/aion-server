package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.ingameshop.IGItem;

/**
 * @author xTz, KID
 */
public class InGameShopDAO {

	private static final Logger log = LoggerFactory.getLogger(InGameShopDAO.class);

	public static final String SELECT_QUERY = "SELECT `object_id`, `item_id`, `item_count`, `item_price`, `category`, `sub_category`, `list`, `sales_ranking`, `item_type`, `gift`, `title_description`, `description` FROM `ingameshop`";
	public static final String DELETE_QUERY = "DELETE FROM `ingameshop` WHERE `item_id`=? AND `category`=? AND `sub_category`=? AND `list`=?";
	public static final String UPDATE_SALES_QUERY = "UPDATE `ingameshop` SET `sales_ranking`=? WHERE `object_id`=?";

	public static Map<Byte, List<IGItem>> loadInGameShopItems() {
		Map<Byte, List<IGItem>> items = new HashMap<>();
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			try (ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					byte category = rset.getByte("category");
					byte subCategory = rset.getByte("sub_category");
					if (subCategory < 3)
						continue;
					int objectId = rset.getInt("object_id");
					int itemId = rset.getInt("item_id");
					long itemCount = rset.getLong("item_count");
					long itemPrice = rset.getLong("item_price");
					int list = rset.getInt("list");
					int salesRanking = rset.getInt("sales_ranking");
					byte itemType = rset.getByte("item_type");
					byte gift = rset.getByte("gift");
					String titleDescription = rset.getString("title_description");
					String description = rset.getString("description");
					if (!items.containsKey(category)) {
						items.put(category, new ArrayList<IGItem>());
					}
					items.get(category).add(
						new IGItem(objectId, itemId, itemCount, itemPrice, category, subCategory, list, salesRanking, itemType, gift, titleDescription,
							description));
				}
			}
		} catch (Exception e) {
			log.error("Could not restore inGameShop data for all from DB: " + e.getMessage(), e);
		}
		return items;
	}

	public static boolean deleteIngameShopItem(int itemId, byte category, byte subCategory, int list) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DELETE_QUERY)) {
			stmt.setInt(1, itemId);
			stmt.setInt(2, category);
			stmt.setInt(3, subCategory);
			stmt.setInt(4, list);
			stmt.execute();
		} catch (Exception e) {
			log.error("Error delete ingameshopItem: " + itemId, e);
			return false;
		}
		return true;
	}

	public static void saveIngameShopItem(int objectId, int itemId, long itemCount, long itemPrice, byte category, byte subCategory, int list,
		int salesRanking, byte itemType, byte gift, String titleDescription, String description) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con
					 .prepareStatement("INSERT INTO ingameshop(object_id, item_id, item_count, item_price, category, sub_category, list, sales_ranking, item_type, gift, title_description, description)"
						 + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
			stmt.setInt(1, objectId);
			stmt.setInt(2, itemId);
			stmt.setLong(3, itemCount);
			stmt.setLong(4, itemPrice);
			stmt.setByte(5, category);
			stmt.setByte(6, subCategory);
			stmt.setInt(7, list);
			stmt.setInt(8, salesRanking);
			stmt.setByte(9, itemType);
			stmt.setByte(10, gift);
			stmt.setString(11, titleDescription);
			stmt.setString(12, description);
			stmt.execute();
		} catch (Exception e) {
			log.error("Error saving Item: " + objectId, e);
		}
	}

	public static boolean increaseSales(int object, int current) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_SALES_QUERY)) {
			stmt.setInt(1, current);
			stmt.setInt(2, object);
			stmt.execute();
		} catch (Exception e) {
			log.error("Error increaseSales Item: " + object, e);
			return false;
		}
		return true;
	}

}
