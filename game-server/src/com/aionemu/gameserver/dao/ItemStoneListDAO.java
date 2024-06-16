package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.utils.GenericValidator;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.GodStone;
import com.aionemu.gameserver.model.items.IdianStone;
import com.aionemu.gameserver.model.items.ItemStone;
import com.aionemu.gameserver.model.items.ItemStone.ItemStoneType;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;

/**
 * @author ATracer, Wakizashi
 */
public class ItemStoneListDAO {

	private static final Logger log = LoggerFactory.getLogger(ItemStoneListDAO.class);

	public static final String INSERT_QUERY = "INSERT INTO `item_stones` (`item_unique_id`, `item_id`, `slot`, `category`, `polishNumber`, `polishCharge`, `proc_count`) VALUES (?,?,?,?,?,?,?)";
	public static final String UPDATE_QUERY = "UPDATE `item_stones` SET `item_id`=?, `slot`=?, `polishNumber`=?, `polishCharge`=?, `proc_count`=? where `item_unique_id`=? AND `category`=?";
	public static final String DELETE_QUERY = "DELETE FROM `item_stones` WHERE `item_unique_id`=? AND slot=? AND category=?";
	public static final String SELECT_QUERY = "SELECT `item_id`, `slot`, `category`, `polishNumber`, `polishCharge`, `proc_count` FROM `item_stones` WHERE `item_unique_id`=?";

	public static void load(Collection<Item> items) {
		List<Item> validItems = items.stream().filter(i -> i.getItemTemplate().isArmor() || i.getItemTemplate().isWeapon()).collect(Collectors.toList());
		if (validItems.isEmpty())
			return;
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			for (Item item : validItems) {
				stmt.setInt(1, item.getObjectId());
				try (ResultSet rset = stmt.executeQuery()) {
					while (rset.next()) {
						int itemId = rset.getInt("item_id");
						int slot = rset.getInt("slot");
						int stoneType = rset.getInt("category");
						int activatedCount = rset.getInt("proc_count");
						switch (stoneType) {
							case 0:
								if (item.getSockets(false) <= item.getItemStonesSize()) {
									log.warn("Deleting manastone " + itemId + " due to slot overload from " + item);
									deleteItemStone(con, item.getObjectId(), slot, stoneType);
									continue;
								}
								if (DataManager.ITEM_DATA.getItemTemplate(itemId).getItemGroup() == ItemGroup.SPECIAL_MANASTONE
									&& slot >= item.getItemTemplate().getSpecialSlots()) {
									log.warn("Deleting special manastone " + itemId + " from normal slot of " + item);
									deleteItemStone(con, item.getObjectId(), slot, stoneType);
									continue;
								}
								item.getItemStones().add(new ManaStone(item.getObjectId(), itemId, slot, PersistentState.UPDATED));
								break;
							case 1:
								item.addGodStone(itemId, activatedCount);
								GodStone godstone = item.getGodStone();
								if (godstone != null)
									godstone.setPersistentState(PersistentState.UPDATED);
								break;
							case 2:
								if (item.getSockets(true) <= item.getFusionStonesSize()) {
									log.warn("Deleting manastone " + itemId + " due to slot overload from fusioned item of " + item);
									deleteItemStone(con, item.getObjectId(), slot, stoneType);
									continue;
								}
								if (DataManager.ITEM_DATA.getItemTemplate(itemId).getItemGroup() == ItemGroup.SPECIAL_MANASTONE
									&& slot >= item.getFusionedItemTemplate().getSpecialSlots()) {
									log.warn("Deleting special manastone " + itemId + " from normal slot of fusioned item of " + item);
									deleteItemStone(con, item.getObjectId(), slot, stoneType);
									continue;
								}
								item.getFusionStones().add(new ManaStone(item.getObjectId(), itemId, slot, PersistentState.UPDATED));
								break;
							case 3:
								item.setIdianStone(
									new IdianStone(itemId, PersistentState.UPDATE_REQUIRED, item, rset.getInt("polishNumber"), rset.getInt("polishCharge")));
								break;
						}
					}
				}
			}
		} catch (Exception e) {
			log.error("Could not restore ItemStoneList data from DB: " + e.getMessage(), e);
		}
	}

	public static void save(List<Item> items) {
		if (GenericValidator.isBlankOrNull(items)) {
			return;
		}

		Set<ManaStone> manaStones = new HashSet<>();
		Set<ManaStone> fusionStones = new HashSet<>();
		Set<GodStone> godStones = new HashSet<>();
		Set<IdianStone> idianStones = new HashSet<>();

		for (Item item : items) {
			if (item.hasManaStones()) {
				manaStones.addAll(item.getItemStones());
			}

			if (item.hasFusionStones()) {
				fusionStones.addAll(item.getFusionStones());
			}

			GodStone godStone = item.getGodStone();
			if (godStone != null) {
				godStones.add(godStone);
			}
			IdianStone idianStone = item.getIdianStone();
			if (idianStone != null) {
				idianStones.add(idianStone);
			}
		}
		store(manaStones, ItemStoneType.MANASTONE);
		store(fusionStones, ItemStoneType.FUSIONSTONE);
		store(godStones, ItemStoneType.GODSTONE);
		store(idianStones, ItemStoneType.IDIANSTONE);
	}

	public static void storeManaStones(Set<ManaStone> manaStones) {
		store(manaStones, ItemStoneType.MANASTONE);
	}

	public static void storeGodStones(GodStone godStones) {
		store(Collections.singleton(godStones), ItemStoneType.GODSTONE);
	}

	public static void storeFusionStone(Set<ManaStone> manaStones) {
		store(manaStones, ItemStoneType.FUSIONSTONE);
	}

	public static void storeIdianStones(IdianStone idianStone) {
		store(Collections.singleton(idianStone), ItemStoneType.IDIANSTONE);
	}

	private static void store(Set<? extends ItemStone> stones, ItemStoneType ist) {
		if (GenericValidator.isBlankOrNull(stones)) {
			return;
		}

		Set<ItemStone> stonesToAdd = stones.stream().filter(Persistable.NEW).collect(Collectors.toSet());
		Set<ItemStone> stonesToDelete = stones.stream().filter(Persistable.DELETED).collect(Collectors.toSet());
		Set<ItemStone> stonesToUpdate = stones.stream().filter(Persistable.CHANGED).collect(Collectors.toSet());

		try (Connection con = DatabaseFactory.getConnection()) {

			con.setAutoCommit(false);

			deleteItemStones(con, stonesToDelete, ist);
			addItemStones(con, stonesToAdd, ist);
			updateItemStones(con, stonesToUpdate, ist);

		} catch (SQLException e) {
			log.error("Can't save stones", e);
		}

		for (ItemStone is : stones) {
			is.setPersistentState(PersistentState.UPDATED);
		}
	}

	private static void addItemStones(Connection con, Collection<ItemStone> itemStones, ItemStoneType ist) {
		if (GenericValidator.isBlankOrNull(itemStones)) {
			return;
		}

		try (PreparedStatement st = con.prepareStatement(INSERT_QUERY)) {
			for (ItemStone is : itemStones) {
				st.setInt(1, is.getItemObjId());
				st.setInt(2, is.getItemId());
				st.setInt(3, is.getSlot());
				st.setInt(4, ist.ordinal());
				if (is instanceof IdianStone) {
					IdianStone stone = (IdianStone) is;
					st.setInt(5, stone.getPolishNumber());
					st.setInt(6, stone.getPolishCharge());
				} else {
					st.setInt(5, 0);
					st.setInt(6, 0);
				}
				if (is instanceof GodStone) {
					GodStone gs = (GodStone) is;
					st.setInt(7, gs.getActivatedCount());
				} else {
					st.setInt(7, 0);
				}

				st.addBatch();
			}

			st.executeBatch();
			con.commit();
		} catch (SQLException e) {
			log.error("Error occured while saving item stones", e);
		}
	}

	private static void updateItemStones(Connection con, Collection<ItemStone> itemStones, ItemStoneType ist) {
		if (GenericValidator.isBlankOrNull(itemStones)) {
			return;
		}

		try (PreparedStatement st = con.prepareStatement(UPDATE_QUERY)) {
			for (ItemStone is : itemStones) {
				st.setInt(1, is.getItemId());
				st.setInt(2, is.getSlot());
				if (is instanceof IdianStone) {
					IdianStone stone = (IdianStone) is;
					st.setInt(3, stone.getPolishNumber());
					st.setInt(4, stone.getPolishCharge());
				} else {
					st.setInt(3, 0);
					st.setInt(4, 0);
				}
				if (is instanceof GodStone) {
					GodStone gs = (GodStone) is;
					st.setInt(5, gs.getActivatedCount());
				} else {
					st.setInt(5, 0);
				}
				st.setInt(6, is.getItemObjId());
				st.setInt(7, ist.ordinal());
				st.addBatch();
			}

			st.executeBatch();
			con.commit();
		} catch (SQLException e) {
			log.error("Error occured while saving item stones", e);
		}
	}

	private static void deleteItemStones(Connection con, Collection<ItemStone> itemStones, ItemStoneType ist) {
		if (GenericValidator.isBlankOrNull(itemStones)) {
			return;
		}

		try (PreparedStatement st = con.prepareStatement(DELETE_QUERY)) {
			// TODO: Shouldn't we update stone slot?
			for (ItemStone is : itemStones) {
				st.setInt(1, is.getItemObjId());
				st.setInt(2, is.getSlot());
				st.setInt(3, ist.ordinal());
				st.execute();
				st.addBatch();
			}

			st.executeBatch();
			con.commit();
		} catch (SQLException e) {
			log.error("Error occured while saving item stones", e);
		}
	}

	private static void deleteItemStone(Connection con, int uid, int slot, int category) {
		try (PreparedStatement st = con.prepareStatement(DELETE_QUERY)) {
			st.setInt(1, uid);
			st.setInt(2, slot);
			st.setInt(3, category);
			st.execute();
		} catch (SQLException e) {
			log.error("Error occured while saving item stones", e);
		}
	}

	/**
	 * Saves stones of player
	 *
	 * @param player
	 *          whos stones we need to save
	 */
	public static void save(Player player) {
		save(player.getAllItems());
	}

}
