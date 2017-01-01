package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.utils.GenericValidator;
import com.aionemu.gameserver.configs.main.EnchantsConfig;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.items.GodStone;
import com.aionemu.gameserver.model.items.IdianStone;
import com.aionemu.gameserver.model.items.ItemStone;
import com.aionemu.gameserver.model.items.ItemStone.ItemStoneType;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;

/**
 * @author ATracer
 */
public class MySQL5ItemStoneListDAO extends ItemStoneListDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5ItemStoneListDAO.class);
	public static final String INSERT_QUERY = "INSERT INTO `item_stones` (`item_unique_id`, `item_id`, `slot`, `category`, `polishNumber`, `polishCharge`, `proc_count`) VALUES (?,?,?,?,?,?,?)";
	public static final String UPDATE_QUERY = "UPDATE `item_stones` SET `item_id`=?, `slot`=?, `polishNumber`=?, `polishCharge`=?, `proc_count`=? where `item_unique_id`=? AND `category`=?";
	public static final String DELETE_QUERY = "DELETE FROM `item_stones` WHERE `item_unique_id`=? AND slot=? AND category=?";
	public static final String SELECT_QUERY = "SELECT `item_id`, `slot`, `category`, `polishNumber`, `polishCharge`, `proc_count` FROM `item_stones` WHERE `item_unique_id`=?";
	private static final Predicate<ItemStone> itemStoneAddPredicate = new Predicate<ItemStone>() {

		@Override
		public boolean test(@Nullable ItemStone itemStone) {
			return itemStone != null && PersistentState.NEW == itemStone.getPersistentState();
		}

	};
	private static final Predicate<ItemStone> itemStoneDeletedPredicate = new Predicate<ItemStone>() {

		@Override
		public boolean test(@Nullable ItemStone itemStone) {
			return itemStone != null && PersistentState.DELETED == itemStone.getPersistentState();
		}

	};
	private static final Predicate<ItemStone> itemStoneUpdatePredicate = new Predicate<ItemStone>() {

		@Override
		public boolean test(@Nullable ItemStone itemStone) {
			return itemStone != null && PersistentState.UPDATE_REQUIRED == itemStone.getPersistentState();
		}

	};

	@Override
	public void load(final Collection<Item> items) {
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
									log.warn("Manastone slots overloaded. ObjectId: " + item.getObjectId());
									if (EnchantsConfig.CLEAN_STONE) {
										deleteItemStone(con, item.getObjectId(), slot, stoneType);
									}
									continue;
								}
								if (DataManager.ITEM_DATA.getItemTemplate(itemId).getItemGroup() == ItemGroup.SPECIAL_MANASTONE
									&& slot >= item.getItemTemplate().getSpecialSlots()) {
									log.warn("Special Manastone in normal slot. ObjectId: " + item.getObjectId());
									if (EnchantsConfig.CLEAN_STONE) {
										deleteItemStone(con, item.getObjectId(), slot, stoneType);
									}
									continue;
								}
								item.getItemStones().add(new ManaStone(item.getObjectId(), itemId, slot, PersistentState.UPDATED));
								break;
							case 1:
								item.setGodStone(new GodStone(item, activatedCount, itemId, PersistentState.UPDATED));
								break;
							case 2:
								if (item.getSockets(true) <= item.getFusionStonesSize()) {
									log.warn("Manastone slots overloaded. ObjectId: " + item.getObjectId());
									if (EnchantsConfig.CLEAN_STONE) {
										deleteItemStone(con, item.getObjectId(), slot, stoneType);
									}
									continue;
								}
								if (DataManager.ITEM_DATA.getItemTemplate(itemId).getItemGroup() == ItemGroup.SPECIAL_MANASTONE
									&& slot >= item.getFusionedItemTemplate().getSpecialSlots()) {
									log.warn("Special Manastone in normal slot. ObjectId: " + item.getObjectId());
									if (EnchantsConfig.CLEAN_STONE) {
										deleteItemStone(con, item.getObjectId(), slot, stoneType);
									}
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

	@Override
	public void save(List<Item> items) {
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

	@Override
	public void storeManaStones(Set<ManaStone> manaStones) {
		store(manaStones, ItemStoneType.MANASTONE);
	}

	@Override
	public void storeGodStones(GodStone godStones) {
		store(Collections.singleton(godStones), ItemStoneType.GODSTONE);
	}

	@Override
	public void storeFusionStone(Set<ManaStone> manaStones) {
		store(manaStones, ItemStoneType.FUSIONSTONE);
	}

	@Override
	public void storeIdianStones(IdianStone idianStone) {
		store(Collections.singleton(idianStone), ItemStoneType.IDIANSTONE);
	}

	private void store(Set<? extends ItemStone> stones, ItemStoneType ist) {
		if (GenericValidator.isBlankOrNull(stones)) {
			return;
		}

		Set<ItemStone> stonesToAdd = stones.stream().filter(itemStoneAddPredicate).collect(Collectors.toSet());
		Set<ItemStone> stonesToDelete = stones.stream().filter(itemStoneDeletedPredicate).collect(Collectors.toSet());
		Set<ItemStone> stonesToUpdate = stones.stream().filter(itemStoneUpdatePredicate).collect(Collectors.toSet());

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

	private void addItemStones(Connection con, Collection<ItemStone> itemStones, ItemStoneType ist) {
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

	private void updateItemStones(Connection con, Collection<ItemStone> itemStones, ItemStoneType ist) {
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

	private void deleteItemStones(Connection con, Collection<ItemStone> itemStones, ItemStoneType ist) {
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

	private void deleteItemStone(Connection con, int uid, int slot, int category) {
		try (PreparedStatement st = con.prepareStatement(DELETE_QUERY)) {
			st.setInt(1, uid);
			st.setInt(2, slot);
			st.setInt(3, category);
			st.execute();
		} catch (SQLException e) {
			log.error("Error occured while saving item stones", e);
		}
	}

	@Override
	public boolean supports(String s, int i, int i1) {
		return MySQL5DAOUtils.supports(s, i, i1);
	}

}
