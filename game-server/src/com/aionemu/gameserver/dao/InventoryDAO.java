package com.aionemu.gameserver.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.utils.GenericValidator;
import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.items.ItemStone;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * @author ATracer
 */
public class InventoryDAO {

	private static final Logger log = LoggerFactory.getLogger(InventoryDAO.class);

	public static final String SELECT_QUERY = "SELECT * FROM `inventory` WHERE `item_owner`=? AND `item_location`=?";
	public static final String SELECT_ALL_QUERY = "SELECT * FROM `inventory` WHERE `item_location`=?";
	public static final String SELECT_EQUIPPED_QUERY = "SELECT i.item_skin, i.slot, i.item_color, s.item_id godstone_item_id FROM inventory i LEFT JOIN item_stones s ON s.item_unique_id = i.item_unique_id AND s.slot = 0 AND s.category = ? WHERE i.item_owner = ? AND i.item_location = ? AND i.is_equipped = 1";
	public static final String INSERT_QUERY = "INSERT INTO `inventory` (`item_unique_id`, `item_id`, `item_count`, `item_color`, `color_expires`, `item_creator`, `expire_time`, `activation_count`, `item_owner`, `is_equipped`, is_soul_bound, `slot`, `item_location`, `enchant`, `enchant_bonus`, `item_skin`, `fusioned_item`, `optional_socket`, `optional_fusion_socket`, `charge`, `tune_count`, `rnd_bonus`, `fusion_rnd_bonus`, `tempering`, `pack_count`, `is_amplified`, `buff_skill`, `rnd_plume_bonus`) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String UPDATE_QUERY = "UPDATE inventory SET item_count=?, item_color=?, color_expires=?, item_creator=?, expire_time=?, activation_count=?, item_owner=?, is_equipped=?, is_soul_bound=?, slot=?, item_location=?, enchant=?, enchant_bonus=?, item_skin=?, fusioned_item=?, optional_socket=?, optional_fusion_socket=?, charge=?, tune_count=?, rnd_bonus=?, fusion_rnd_bonus=?, tempering=?, pack_count=?, is_amplified=?, buff_skill=?, rnd_plume_bonus=? WHERE item_unique_id=?";
	public static final String DELETE_QUERY = "DELETE FROM inventory WHERE item_unique_id=?";
	public static final String DELETE_CLEAN_QUERY = "DELETE FROM inventory WHERE item_owner=? AND item_location != 2"; // exclude acc wh since item_owner (acc id) is no idfactory id
	public static final String SELECT_ACCOUNT_QUERY = "SELECT `account_id` FROM `players` WHERE `id`=?";
	public static final String SELECT_LEGION_QUERY = "SELECT `legion_id` FROM `legion_members` WHERE `player_id`=?";
	public static final String DELETE_ACCOUNT_WH = "DELETE FROM inventory WHERE item_owner=? AND item_location=2";

	public static void loadStorage(int ownerId, Storage storage) {
		loadItems(ownerId, storage.getStorageType(), storage::onLoadHandler);
	}

	public static List<Item> loadItems(int ownerId, StorageType storageType) {
		List<Item> items = new ArrayList<>();
		loadItems(ownerId, storageType, items::add);
		return items;
	}

	private static void loadItems(int ownerId, StorageType storageType, Consumer<Item> itemConsumer) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, ownerId);
			stmt.setInt(2, storageType.getId());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Item item = constructItem(storageType.getId(), rs);
				item.setPersistentState(PersistentState.UPDATED);
				itemConsumer.accept(item);
			}
		} catch (Exception e) {
			log.error("Could not load " + storageType + " items of owner " + ownerId, e);
		}
	}

	public static List<Item> loadBrokerItems() {
		List<Item> items = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_ALL_QUERY)) {
			stmt.setInt(1, StorageType.BROKER.getId());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				Item item = constructItem(StorageType.BROKER.getId(), rs);
				item.setPersistentState(PersistentState.UPDATED);
				items.add(item);
			}
		} catch (Exception e) {
			log.error("Could not load broker items", e);
		}
		return items;
	}

	public static List<PlayerAccountData.VisibleItem> loadVisibleEquipment(int ownerId) {
		List<PlayerAccountData.VisibleItem> items = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_EQUIPPED_QUERY)) {
			stmt.setInt(1, ItemStone.ItemStoneType.GODSTONE.ordinal());
			stmt.setInt(2, ownerId);
			stmt.setInt(3, StorageType.CUBE.getId());
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				byte slotType = ItemSlot.getEquipmentSlotType(rs.getLong("slot"));
				if (slotType == 0)
					continue; // skip equipment like rings and secondary weapons, as they are not visible and AbstractPlayerInfoPacket supports only 16 items
				int itemSkinId = rs.getInt("item_skin");
				int godStoneItemId = rs.getInt("godstone_item_id");
				Integer itemColor = (Integer) rs.getObject("item_color");
				items.add(new PlayerAccountData.VisibleItem(slotType, itemSkinId, godStoneItemId, itemColor));
			}
		} catch (Exception e) {
			log.error("Could not load equipped items of owner " + ownerId, e);
		}
		return items;
	}

	private static Item constructItem(int storage, ResultSet rset) throws SQLException {
		int itemUniqueId = rset.getInt("item_unique_id");
		int itemId = rset.getInt("item_id");
		long itemCount = rset.getLong("item_count");
		Integer itemColor = (Integer) rset.getObject("item_color"); // accepts null (which means not dyed)
		int colorExpireTime = rset.getInt("color_expires");
		String itemCreator = rset.getString("item_creator");
		int expireTime = rset.getInt("expire_time");
		int activationCount = rset.getInt("activation_count");
		int isEquiped = rset.getInt("is_equipped");
		int isSoulBound = rset.getInt("is_soul_bound");
		long slot = rset.getLong("slot");
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

		return new Item(itemUniqueId, itemId, itemCount, itemColor, colorExpireTime, itemCreator, expireTime, activationCount, isEquiped == 1,
			isSoulBound == 1, slot, storage, enchant, enchantBonus, itemSkin, fusionedItem, optionalSocket, optionalFusionSocket, charge, tuneCount,
			bonusStatsId, fusionedItemBonusStatsId, tempering, packCount, isAmplified == 1, buffSkill, rndPlumeBonusValue);
	}

	private static int loadPlayerAccountId(int playerId) {
		int accountId = 0;
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_ACCOUNT_QUERY)) {
			stmt.setInt(1, playerId);
			try (ResultSet rset = stmt.executeQuery()) {
				if (rset.next()) {
					accountId = rset.getInt("account_id");
				}
			}
		} catch (Exception e) {
			log.error("Could not restore accountId data for player: " + playerId + " from DB: " + e.getMessage(), e);
		}
		return accountId;
	}

	public static int loadLegionId(int playerId) {
		int legionId = 0;
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_LEGION_QUERY)) {
			stmt.setInt(1, playerId);
			try (ResultSet rset = stmt.executeQuery()) {
				if (rset.next()) {
					legionId = rset.getInt("legion_id");
				}
			}
		} catch (Exception e) {
			log.error("Failed to load legion id for player id: " + playerId, e);
		}
		return legionId;
	}

	public static boolean store(Item item, int playerId) {
		return store(Arrays.asList(item), playerId);
	}

	public static boolean store(Item item, Integer playerId, Integer accountId, Integer legionId) {
		return store(Arrays.asList(item), playerId, accountId, legionId);
	}

	public static boolean store(Player player) {
		int playerId = player.getObjectId();
		Integer accountId = player.getAccount() != null ? player.getAccount().getId() : null;
		Integer legionId = player.getLegion() != null ? player.getLegion().getLegionId() : null;

		List<Item> allPlayerItems = player.getDirtyItemsToUpdate();
		return store(allPlayerItems, playerId, accountId, legionId);
	}

	public static boolean store(Item item, Player player) {
		int playerId = player.getObjectId();
		int accountId = player.getAccount().getId();
		Integer legionId = player.getLegion() != null ? player.getLegion().getLegionId() : null;

		return store(item, playerId, accountId, legionId);
	}

	public static boolean store(List<Item> items, int playerId) {

		Integer accountId = null;
		Integer legionId = null;

		for (Item item : items) {

			if (accountId == null && item.getItemLocation() == StorageType.ACCOUNT_WAREHOUSE.getId()) {
				accountId = loadPlayerAccountId(playerId);
			}

			if (legionId == null && item.getItemLocation() == StorageType.LEGION_WAREHOUSE.getId()) {
				int localLegionId = loadLegionId(playerId);
				if (localLegionId > 0)
					legionId = localLegionId;
			}
		}

		return store(items, playerId, accountId, legionId);
	}

	public static boolean store(List<Item> items, Integer playerId, Integer accountId, Integer legionId) {
		Collection<Item> itemsToUpdate = items.stream().filter(Persistable.CHANGED).collect(Collectors.toList());
		Collection<Item> itemsToInsert = items.stream().filter(Persistable.NEW).collect(Collectors.toList());
		Collection<Item> itemsToDelete = items.stream().filter(Persistable.DELETED).collect(Collectors.toList());

		boolean deleteResult = false;
		boolean insertResult = false;
		boolean updateResult = false;

		try (Connection con = DatabaseFactory.getConnection()) {
			con.setAutoCommit(false);
			deleteResult = deleteItems(con, itemsToDelete);
			insertResult = insertItems(con, itemsToInsert, playerId, accountId, legionId);
			updateResult = updateItems(con, itemsToUpdate, playerId, accountId, legionId);
		} catch (SQLException e) {
			log.error("Can't save inventory for player: " + playerId, e);
		}

		for (Item item : items) {
			item.setPersistentState(PersistentState.UPDATED);
		}

		if (deleteResult)
			IDFactory.getInstance().releaseObjectIds(itemsToDelete);

		return deleteResult && insertResult && updateResult;
	}

	private static int getItemOwnerId(Item item, Integer playerId, Integer accountId, Integer legionId) {
		if (item.getItemLocation() == StorageType.ACCOUNT_WAREHOUSE.getId()) {
			return accountId;
		}

		if (item.getItemLocation() == StorageType.LEGION_WAREHOUSE.getId()) {
			return legionId != null ? legionId : playerId;
		}

		return playerId;
	}

	private static boolean insertItems(Connection con, Collection<Item> items, Integer playerId, Integer accountId, Integer legionId) {

		if (GenericValidator.isBlankOrNull(items)) {
			return true;
		}

		try (PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
			for (Item item : items) {
				stmt.setInt(1, item.getObjectId());
				stmt.setInt(2, item.getItemTemplate().getTemplateId());
				stmt.setLong(3, item.getItemCount());
				stmt.setObject(4, item.getItemColor(), Types.INTEGER); // supports inserting null value
				stmt.setInt(5, item.getColorExpireTime());
				stmt.setString(6, item.getItemCreator());
				stmt.setInt(7, item.getExpireTime());
				stmt.setInt(8, item.getActivationCount());
				stmt.setInt(9, getItemOwnerId(item, playerId, accountId, legionId));
				stmt.setBoolean(10, item.isEquipped());
				stmt.setInt(11, item.isSoulBound() ? 1 : 0);
				stmt.setLong(12, item.getEquipmentSlot());
				stmt.setInt(13, item.getItemLocation());
				stmt.setInt(14, item.getEnchantLevel());
				stmt.setInt(15, item.getEnchantBonus());
				stmt.setInt(16, item.getItemSkinTemplate().getTemplateId());
				stmt.setInt(17, item.getFusionedItemId());
				stmt.setInt(18, item.getOptionalSockets());
				stmt.setInt(19, item.getFusionedItemOptionalSockets());
				stmt.setInt(20, item.getChargePoints());
				stmt.setInt(21, item.getTuneCount());
				stmt.setInt(22, item.getBonusStatsId());
				stmt.setInt(23, item.getFusionedItemBonusStatsId());
				stmt.setInt(24, item.getTempering());
				stmt.setInt(25, item.getPackCount());
				stmt.setBoolean(26, item.isAmplified());
				stmt.setInt(27, item.getBuffSkill());
				stmt.setInt(28, item.getRndPlumeBonusValue());
				stmt.addBatch();
			}

			stmt.executeBatch();
			con.commit();
		} catch (Exception e) {
			log.error("Failed to execute insert batch", e);
			return false;
		}
		return true;
	}

	private static boolean updateItems(Connection con, Collection<Item> items, Integer playerId, Integer accountId, Integer legionId) {

		if (GenericValidator.isBlankOrNull(items)) {
			return true;
		}

		try (PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY)) {
			for (Item item : items) {
				stmt.setLong(1, item.getItemCount());
				stmt.setObject(2, item.getItemColor(), Types.INTEGER); // supports inserting null value
				stmt.setInt(3, item.getColorExpireTime());
				stmt.setString(4, item.getItemCreator());
				stmt.setInt(5, item.getExpireTime());
				stmt.setInt(6, item.getActivationCount());
				stmt.setInt(7, getItemOwnerId(item, playerId, accountId, legionId));
				stmt.setBoolean(8, item.isEquipped());
				stmt.setInt(9, item.isSoulBound() ? 1 : 0);
				stmt.setLong(10, item.getEquipmentSlot());
				stmt.setInt(11, item.getItemLocation());
				stmt.setInt(12, item.getEnchantLevel());
				stmt.setInt(13, item.getEnchantBonus());
				stmt.setInt(14, item.getItemSkinTemplate().getTemplateId());
				stmt.setInt(15, item.getFusionedItemId());
				stmt.setInt(16, item.getOptionalSockets());
				stmt.setInt(17, item.getFusionedItemOptionalSockets());
				stmt.setInt(18, item.getChargePoints());
				stmt.setInt(19, item.getTuneCount());
				stmt.setInt(20, item.getBonusStatsId());
				stmt.setInt(21, item.getFusionedItemBonusStatsId());
				stmt.setInt(22, item.getTempering());
				stmt.setInt(23, item.getPackCount());
				stmt.setBoolean(24, item.isAmplified());
				stmt.setInt(25, item.getBuffSkill());
				stmt.setInt(26, item.getRndPlumeBonusValue());
				stmt.setInt(27, item.getObjectId());
				stmt.addBatch();
			}

			stmt.executeBatch();
			con.commit();
		} catch (Exception e) {
			log.error("Failed to execute update batch", e);
			return false;
		}
		return true;
	}

	private static boolean deleteItems(Connection con, Collection<Item> items) {

		if (GenericValidator.isBlankOrNull(items)) {
			return true;
		}

		try (PreparedStatement stmt = con.prepareStatement(DELETE_QUERY)) {
			for (Item item : items) {
				stmt.setInt(1, item.getObjectId());
				stmt.addBatch();
			}

			stmt.executeBatch();
			con.commit();
		} catch (Exception e) {
			log.error("Failed to execute delete batch", e);
			return false;
		}
		return true;
	}

	/**
	 * Since inventory is not using FK - need to clean items
	 */
	public static boolean deletePlayerOrLegionItems(int playerOrLegionId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DELETE_CLEAN_QUERY)) {
			stmt.setInt(1, playerOrLegionId);
			stmt.execute();
		} catch (Exception e) {
			log.error("Error deleting all player or legion items. playerOrLegionId: " + playerOrLegionId, e);
			return false;
		}
		return true;
	}

	public static void deleteAccountWH(int accountId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DELETE_ACCOUNT_WH)) {
			stmt.setInt(1, accountId);
			stmt.execute();
		} catch (Exception e) {
			log.error("Error deleting all items from account WH. AccountId: " + accountId, e);
		}
	}

	public static int[] getUsedIDs() {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("SELECT item_unique_id FROM inventory", ResultSet.TYPE_SCROLL_INSENSITIVE,
					 ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = stmt.executeQuery();
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			int[] ids = new int[count];
			for (int i = 0; rs.next(); i++)
				ids[i] = rs.getInt("item_unique_id");
			return ids;
		} catch (SQLException e) {
			log.error("Can't get list of IDs from inventory table", e);
			return null;
		}
	}

}
