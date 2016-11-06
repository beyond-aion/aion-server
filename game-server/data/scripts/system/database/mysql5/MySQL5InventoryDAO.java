package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.utils.GenericValidator;
import com.aionemu.gameserver.dao.InventoryDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Equipment;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.PlayerStorage;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.services.item.ItemService;

import javolution.util.FastTable;

/**
 * @author ATracer
 */
public class MySQL5InventoryDAO extends InventoryDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5InventoryDAO.class);
	public static final String SELECT_QUERY = "SELECT `item_unique_id`, `item_id`, `item_count`, `item_color`, `color_expires`, `item_creator`, `expire_time`, `activation_count`, `is_equiped`, `is_soul_bound`, `slot`, `enchant`, `enchant_bonus`, `item_skin`, `fusioned_item`, `optional_socket`, `optional_fusion_socket`, `charge`, `rnd_bonus`, `rnd_count`, `tempering`, `pack_count`, `is_amplified`, `buff_skill`, `rnd_plume_bonus` FROM `inventory` WHERE `item_owner`=? AND `item_location`=? AND `is_equiped`=?";
	public static final String INSERT_QUERY = "INSERT INTO `inventory` (`item_unique_id`, `item_id`, `item_count`, `item_color`, `color_expires`, `item_creator`, `expire_time`, `activation_count`, `item_owner`, `is_equiped`, is_soul_bound, `slot`, `item_location`, `enchant`, `enchant_bonus`, `item_skin`, `fusioned_item`, `optional_socket`, `optional_fusion_socket`, `charge`, `rnd_bonus`, `rnd_count`, `tempering`, `pack_count`, `is_amplified`, `buff_skill`, `rnd_plume_bonus`) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String UPDATE_QUERY = "UPDATE inventory SET item_count=?, item_color=?, color_expires=?, item_creator=?, expire_time=?, activation_count=?, item_owner=?, is_equiped=?, is_soul_bound=?, slot=?, item_location=?, enchant=?, enchant_bonus=?, item_skin=?, fusioned_item=?, optional_socket=?, optional_fusion_socket=?, charge=?, rnd_bonus=?, rnd_count=?, tempering=?, pack_count=?, is_amplified=?, buff_skill=?, rnd_plume_bonus=? WHERE item_unique_id=?";
	public static final String DELETE_QUERY = "DELETE FROM inventory WHERE item_unique_id=?";
	public static final String DELETE_CLEAN_QUERY = "DELETE FROM inventory WHERE item_owner=? AND item_location != 2"; // exclude acc wh since item_owner (acc id) is no idfactory id
	public static final String SELECT_ACCOUNT_QUERY = "SELECT `account_id` FROM `players` WHERE `id`=?";
	public static final String SELECT_LEGION_QUERY = "SELECT `legion_id` FROM `legion_members` WHERE `player_id`=?";
	public static final String DELETE_ACCOUNT_WH = "DELETE FROM inventory WHERE item_owner=? AND item_location=2";
	public static final String SELECT_QUERY2 = "SELECT * FROM `inventory` WHERE `item_owner`=? AND `item_location`=?";

	private static final Predicate<Item> itemsToInsertPredicate = new Predicate<Item>() {

		@Override
		public boolean test(@Nullable Item input) {
			return input != null && PersistentState.NEW == input.getPersistentState();
		}
	};

	private static final Predicate<Item> itemsToUpdatePredicate = new Predicate<Item>() {

		@Override
		public boolean test(@Nullable Item input) {
			return input != null && PersistentState.UPDATE_REQUIRED == input.getPersistentState();
		}
	};

	private static final Predicate<Item> itemsToDeletePredicate = new Predicate<Item>() {

		@Override
		public boolean test(@Nullable Item input) {
			return input != null && PersistentState.DELETED == input.getPersistentState();
		}
	};

	@Override
	public Storage loadStorage(int ownerId, StorageType storageType) {
		final Storage inventory = new PlayerStorage(storageType);
		final int storage = storageType.getId();
		final int equipped = 0;
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, ownerId);
			stmt.setInt(2, storage);
			stmt.setInt(3, equipped);
			try (ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					Item item = constructItem(storage, rset);
					item.setPersistentState(PersistentState.UPDATED);
					if (item.getItemTemplate() == null) {
						log.error(ownerId + "loaded error item, itemUniqueId is: " + item.getObjectId());
					} else {
						inventory.onLoadHandler(item);
					}
				}
			}
		} catch (Exception e) {
			log.error("Could not restore " + storageType + " data for owner: " + ownerId + " from DB: " + e.getMessage(), e);
		}
		return inventory;
	}

	@Override
	public List<Item> loadStorageDirect(int ownerId, StorageType storageType) {
		List<Item> list = new FastTable<>();
		final int storage = storageType.getId();
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY2)) {
				stmt.setInt(1, ownerId);
				stmt.setInt(2, storageType.getId());
				try (ResultSet rset = stmt.executeQuery()) {
					while (rset.next()) {
						list.add(constructItem(storage, rset));
					}
				}
			}
		} catch (Exception e) {
			log.error("Could not restore " + storageType + " data for owner: " + ownerId + " from DB: " + e.getMessage(), e);
		}
		return list;
	}

	@Override
	public Equipment loadEquipment(Player player) {
		final Equipment equipment = new Equipment(player);
		final int storage = 0;
		final int equipped = 1;

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, player.getObjectId());
			stmt.setInt(2, storage);
			stmt.setInt(3, equipped);
			try (ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					Item item = constructItem(storage, rset);
					item.setPersistentState(PersistentState.UPDATED);
					equipment.onLoadHandler(item);
				}
			}
		} catch (Exception e) {
			log.error("Could not restore Equipment data for player: " + player.getObjectId() + " from DB: " + e.getMessage(), e);
		}
		return equipment;
	}

	@Override
	public List<Item> loadEquipment(int playerId) {
		final List<Item> items = new FastTable<>();
		final int storage = 0;
		final int equipped = 1;

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, playerId);
			stmt.setInt(2, storage);
			stmt.setInt(3, equipped);
			try (ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					Item item = constructItem(storage, rset);
					items.add(item);
				}
			}
		} catch (Exception e) {
			log.error("Could not restore Equipment data for player: " + playerId + " from DB: " + e.getMessage(), e);
		}
		return items;
	}

	private Item constructItem(final int storage, ResultSet rset) throws SQLException {
		int itemUniqueId = rset.getInt("item_unique_id");
		int itemId = rset.getInt("item_id");
		long itemCount = rset.getLong("item_count");
		Integer itemColor = (Integer) rset.getObject("item_color"); // accepts null (which means not dyed)
		int colorExpireTime = rset.getInt("color_expires");
		String itemCreator = rset.getString("item_creator");
		int expireTime = rset.getInt("expire_time");
		int activationCount = rset.getInt("activation_count");
		int isEquiped = rset.getInt("is_equiped");
		int isSoulBound = rset.getInt("is_soul_bound");
		long slot = rset.getLong("slot");
		int enchant = rset.getInt("enchant");
		int enchantBonus = rset.getInt("enchant_bonus");
		int itemSkin = rset.getInt("item_skin");
		int fusionedItem = rset.getInt("fusioned_item");
		int optionalSocket = rset.getInt("optional_socket");
		int optionalFusionSocket = rset.getInt("optional_fusion_socket");
		int charge = rset.getInt("charge");
		int randomBonus = rset.getInt("rnd_bonus");
		int rndCount = rset.getInt("rnd_count");
		int tempering = rset.getInt("tempering");
		int packCount = rset.getInt("pack_count");
		int isAmplified = rset.getInt("is_amplified");
		int buffSkill = rset.getInt("buff_skill");
		int rndPlumeBonusValue = rset.getInt("rnd_plume_bonus");

		Item item = new Item(itemUniqueId, itemId, itemCount, itemColor, colorExpireTime, itemCreator, expireTime, activationCount, isEquiped == 1,
			isSoulBound == 1, slot, storage, enchant, enchantBonus, itemSkin, fusionedItem, optionalSocket, optionalFusionSocket, charge, randomBonus,
			rndCount, tempering, packCount, isAmplified == 1, buffSkill, rndPlumeBonusValue);
		return item;
	}

	private int loadPlayerAccountId(final int playerId) {
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

	public int loadLegionId(final int playerId) {
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

	@Override
	public boolean store(Player player) {
		int playerId = player.getObjectId();
		Integer accountId = player.getPlayerAccount() != null ? player.getPlayerAccount().getId() : null;
		Integer legionId = player.getLegion() != null ? player.getLegion().getLegionId() : null;

		List<Item> allPlayerItems = player.getDirtyItemsToUpdate();
		return store(allPlayerItems, playerId, accountId, legionId);
	}

	@Override
	public boolean store(Item item, Player player) {
		int playerId = player.getObjectId();
		int accountId = player.getPlayerAccount().getId();
		Integer legionId = player.getLegion() != null ? player.getLegion().getLegionId() : null;

		return store(item, playerId, accountId, legionId);
	}

	@Override
	public boolean store(List<Item> items, int playerId) {

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

	@Override
	public boolean store(List<Item> items, Integer playerId, Integer accountId, Integer legionId) {
		Collection<Item> itemsToUpdate = items.stream().filter(itemsToUpdatePredicate).collect(Collectors.toList());
		Collection<Item> itemsToInsert = items.stream().filter(itemsToInsertPredicate).collect(Collectors.toList());
		Collection<Item> itemsToDelete = items.stream().filter(itemsToDeletePredicate).collect(Collectors.toList());

		boolean deleteResult = false;
		boolean insertResult = false;
		boolean updateResult = false;

		try (Connection con = DatabaseFactory.getConnection()) {
			con.setAutoCommit(false);
			deleteResult = deleteItems(con, itemsToDelete);
			insertResult = insertItems(con, itemsToInsert, playerId, accountId, legionId);
			updateResult = updateItems(con, itemsToUpdate, playerId, accountId, legionId);
		} catch (SQLException e) {
			log.error("Can't open connection to save player inventory: " + playerId);
		}

		for (Item item : items) {
			item.setPersistentState(PersistentState.UPDATED);
		}

		if (!itemsToDelete.isEmpty() && deleteResult) {
			ItemService.releaseItemIds(itemsToDelete);
		}

		return deleteResult && insertResult && updateResult;
	}

	private int getItemOwnerId(Item item, Integer playerId, Integer accountId, Integer legionId) {
		if (item.getItemLocation() == StorageType.ACCOUNT_WAREHOUSE.getId()) {
			return accountId;
		}

		if (item.getItemLocation() == StorageType.LEGION_WAREHOUSE.getId()) {
			return legionId != null ? legionId : playerId;
		}

		return playerId;
	}

	private boolean insertItems(Connection con, Collection<Item> items, Integer playerId, Integer accountId, Integer legionId) {

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
				stmt.setInt(18, item.getOptionalSocket());
				stmt.setInt(19, item.getOptionalFusionSocket());
				stmt.setInt(20, item.getChargePoints());
				stmt.setInt(21, item.getBonusNumber());
				stmt.setInt(22, item.getRandomCount());
				stmt.setInt(23, item.getTempering());
				stmt.setInt(24, item.getPackCount());
				stmt.setBoolean(25, item.isAmplified());
				stmt.setInt(26, item.getBuffSkill());
				stmt.setInt(27, item.getRndPlumeBonusValue());
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

	private boolean updateItems(Connection con, Collection<Item> items, Integer playerId, Integer accountId, Integer legionId) {

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
				stmt.setInt(16, item.getOptionalSocket());
				stmt.setInt(17, item.getOptionalFusionSocket());
				stmt.setInt(18, item.getChargePoints());
				stmt.setInt(19, item.getBonusNumber());
				stmt.setInt(20, item.getRandomCount());
				stmt.setInt(21, item.getTempering());
				stmt.setInt(22, item.getPackCount());
				stmt.setBoolean(23, item.isAmplified());
				stmt.setInt(24, item.getBuffSkill());
				stmt.setInt(25, item.getRndPlumeBonusValue());
				stmt.setInt(26, item.getObjectId());
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

	private boolean deleteItems(Connection con, Collection<Item> items) {

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
	@Override
	public boolean deletePlayerItems(final int playerId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DELETE_CLEAN_QUERY)) {
			stmt.setInt(1, playerId);
			stmt.execute();
		} catch (Exception e) {
			log.error("Error deleting all player items. PlayerObjId: " + playerId, e);
			return false;
		}
		return true;
	}

	@Override
	public void deleteAccountWH(final int accountId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DELETE_ACCOUNT_WH)) {
			stmt.setInt(1, accountId);
			stmt.execute();
		} catch (Exception e) {
			log.error("Error deleting all items from account WH. AccountId: " + accountId, e);
		}
	}

	@Override
	public int[] getUsedIDs() {
		PreparedStatement statement = DB.prepareStatement("SELECT item_unique_id FROM inventory", ResultSet.TYPE_SCROLL_INSENSITIVE,
			ResultSet.CONCUR_READ_ONLY);

		try {
			ResultSet rs = statement.executeQuery();
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			int[] ids = new int[count];
			for (int i = 0; i < count; i++) {
				rs.next();
				ids[i] = rs.getInt("item_unique_id");
			}
			return ids;
		} catch (SQLException e) {
			log.error("Can't get list of id's from inventory table", e);
		} finally {
			DB.close(statement);
		}

		return new int[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supports(String s, int i, int i1) {
		return MySQL5DAOUtils.supports(s, i, i1);
	}
}
