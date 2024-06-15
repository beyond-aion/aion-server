package com.aionemu.gameserver.dao;

import java.sql.*;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.gameobjects.HouseDecoration;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.house.HouseRegistry;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.services.item.HouseObjectFactory;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;

/**
 * @author Rolandas
 */
public class PlayerRegisteredItemsDAO {

	private static final Logger log = LoggerFactory.getLogger(PlayerRegisteredItemsDAO.class);

	public static final String CLEAN_PLAYER_QUERY = "DELETE FROM `player_registered_items` WHERE `player_id` = ?";
	public static final String SELECT_QUERY = "SELECT * FROM `player_registered_items` WHERE `player_id`=?";
	public static final String INSERT_QUERY = "INSERT INTO `player_registered_items` "
		+ "(`expire_time`,`color`,`color_expires`,`owner_use_count`,`visitor_use_count`,`x`,`y`,`z`,`h`,`area`,`room`,`player_id`,`item_unique_id`,`item_id`) VALUES "
		+ "(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
	public static final String UPDATE_QUERY = "UPDATE `player_registered_items` SET "
		+ "`expire_time`=?,`color`=?,`color_expires`=?,`owner_use_count`=?,`visitor_use_count`=?,`x`=?,`y`=?,`z`=?,`h`=?,`area`=?,`room`=? "
		+ "WHERE `player_id`=? AND `item_unique_id`=? AND `item_id`=?";
	public static final String DELETE_QUERY = "DELETE FROM `player_registered_items` WHERE `item_unique_id` = ?";
	public static final String RESET_QUERY = "UPDATE `player_registered_items` SET x=0,y=0,z=0,h=0,area='NONE' WHERE `player_id`=? AND `area` != 'DECOR'";

	public static int[] getUsedIDs() {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("SELECT item_unique_id FROM player_registered_items WHERE item_unique_id <> 0",
					 ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = stmt.executeQuery();
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			int[] ids = new int[count];
			for (int i = 0; rs.next(); i++)
				ids[i] = rs.getInt("item_unique_id");
			return ids;
		} catch (SQLException e) {
			log.error("Can't get list of IDs from player_registered_items table", e);
			return null;
		}
	}

	public static void loadRegistry(HouseRegistry registry) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, registry.getOwner().getOwnerId());
			try (ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					String area = rset.getString("area");
					if ("DECOR".equals(area)) {
						registry.putDecor(createDecoration(registry, rset), false);
					} else {
						registry.putObject(constructObject(registry, rset), false);
					}
				}
				boolean hasInvalidDecors = registry.getDecors().stream().anyMatch(decor -> decor.getPersistentState() == PersistentState.DELETED);
				registry.setPersistentState(hasInvalidDecors ? PersistentState.UPDATE_REQUIRED : PersistentState.UPDATED);
			}
		} catch (Exception e) {
			log.error("Could not load house registry data for player " + registry.getOwner().getOwnerId(), e);
		}
	}

	private static HouseObject<?> constructObject(HouseRegistry registry, ResultSet rset) throws SQLException, IllegalAccessException {
		int itemUniqueId = rset.getInt("item_unique_id");
		VisibleObject visObj = World.getInstance().findVisibleObject(itemUniqueId);
		HouseObject<?> obj;
		if (visObj != null) {
			if (visObj instanceof HouseObject<?>)
				obj = (HouseObject<?>) visObj;
			else {
				throw new IllegalAccessException("Someone stole my house object id : " + itemUniqueId);
			}
		} else {
			obj = registry.getObjectByObjId(itemUniqueId);
			if (obj == null)
				obj = HouseObjectFactory.createNew(registry, itemUniqueId, rset.getInt("item_id"));
		}
		obj.setOwnerUsedCount(rset.getInt("owner_use_count"));
		obj.setVisitorUsedCount(rset.getInt("visitor_use_count"));
		obj.setX(rset.getFloat("x"));
		obj.setY(rset.getFloat("y"));
		obj.setZ(rset.getFloat("z"));
		obj.setHeading((byte) rset.getInt("h"));
		obj.setColor((Integer) rset.getObject("color"));
		obj.setColorExpireEnd(rset.getInt("color_expires"));
		if (obj.getObjectTemplate().getUseDays() > 0)
			obj.setExpireTime(rset.getInt("expire_time"));
		obj.setPersistentState(PersistentState.UPDATED);
		return obj;
	}

	private static HouseDecoration createDecoration(HouseRegistry registry, ResultSet rset) throws SQLException {
		int itemUniqueId = rset.getInt("item_unique_id");
		int itemId = rset.getInt("item_id");
		byte room = rset.getByte("room");
		HouseDecoration decor = new HouseDecoration(itemUniqueId, itemId, room);

		if (!decor.getTemplate().isForBuilding(registry.getOwner().getBuilding())
			|| decor.getRoom() > 0 && registry.getOwner().getHouseType() != HouseType.PALACE)
			decor.setPersistentState(PersistentState.DELETED);
		else
			decor.setPersistentState(PersistentState.UPDATED);

		return decor;
	}

	public static boolean store(HouseRegistry registry, int playerId) {
		List<HouseObject<?>> objects = registry.getObjects();
		List<HouseDecoration> decors = registry.getDecors();
		List<HouseObject<?>> objectsToAdd = objects.stream().filter(Persistable.NEW).collect(Collectors.toList());
		List<HouseObject<?>> objectsToUpdate = objects.stream().filter(Persistable.CHANGED).collect(Collectors.toList());
		List<HouseObject<?>> objectsToDelete = objects.stream().filter(Persistable.DELETED).collect(Collectors.toList());
		List<HouseDecoration> decorsToAdd = decors.stream().filter(Persistable.NEW).collect(Collectors.toList());
		List<HouseDecoration> decorsToUpdate = decors.stream().filter(Persistable.CHANGED).collect(Collectors.toList());
		List<HouseDecoration> decorsToDelete = decors.stream().filter(Persistable.DELETED).collect(Collectors.toList());

		boolean objectDeleteResult = false;
		boolean decorsDeleteResult = false;

		try (Connection con = DatabaseFactory.getConnection()) {
			con.setAutoCommit(false);
			objectDeleteResult = deleteObjects(con, objectsToDelete);
			decorsDeleteResult = deleteDecors(con, decorsToDelete);
			storeObjects(con, objectsToUpdate, playerId, false);
			storeDecors(con, decorsToUpdate, playerId, false);
			storeObjects(con, objectsToAdd, playerId, true);
			storeDecors(con, decorsToAdd, playerId, true);
			registry.setPersistentState(PersistentState.UPDATED);
		} catch (SQLException e) {
			log.error("Can't save registered items for player: " + playerId, e);
		}

		for (HouseObject<?> obj : objects) {
			if (obj.getPersistentState() == PersistentState.DELETED)
				registry.discardObject(obj, true);
			else
				obj.setPersistentState(PersistentState.UPDATED);
		}

		for (HouseDecoration decor : decors) {
			if (decor.getPersistentState() == PersistentState.DELETED)
				registry.discardDecor(decor, true);
			else
				decor.setPersistentState(PersistentState.UPDATED);
		}

		if (objectDeleteResult)
			IDFactory.getInstance().releaseObjectIds(objectsToDelete);

		if (decorsDeleteResult)
			IDFactory.getInstance().releaseObjectIds(decorsToDelete);

		return true;
	}

	private static boolean storeObjects(Connection con, Collection<HouseObject<?>> objects, int playerId, boolean isNew) {
		if (objects.isEmpty())
			return true;

		try (PreparedStatement stmt = con.prepareStatement(isNew ? INSERT_QUERY : UPDATE_QUERY)) {
			for (HouseObject<?> obj : objects) {
				stmt.setObject(1, obj.getExpireTime() > 0 ? obj.getExpireTime() : null, Types.INTEGER);
				stmt.setObject(2, obj.getColor(), Types.INTEGER);
				stmt.setInt(3, obj.getColorExpireEnd());
				stmt.setInt(4, obj.getOwnerUsedCount());
				stmt.setInt(5, obj.getVisitorUsedCount());
				stmt.setFloat(6, obj.getX());
				stmt.setFloat(7, obj.getY());
				stmt.setFloat(8, obj.getZ());
				stmt.setInt(9, obj.getHeading());
				if (obj.getX() > 0 || obj.getY() > 0 || obj.getZ() > 0)
					stmt.setString(10, obj.getPlaceArea().toString());
				else
					stmt.setString(10, "NONE");
				stmt.setByte(11, (byte) 0);
				stmt.setInt(12, playerId);
				stmt.setInt(13, obj.getObjectId());
				stmt.setInt(14, obj.getObjectTemplate().getTemplateId());
				stmt.addBatch();
			}

			stmt.executeBatch();
			con.commit();
		} catch (Exception e) {
			log.error("Failed to execute house object update batch", e);
			return false;
		}
		return true;
	}

	private static boolean storeDecors(Connection con, Collection<HouseDecoration> decors, int playerId, boolean isNew) {
		if (decors.isEmpty())
			return true;

		try (PreparedStatement stmt = con.prepareStatement(isNew ? INSERT_QUERY : UPDATE_QUERY)) {
			for (HouseDecoration decor : decors) {
				stmt.setNull(1, Types.INTEGER);
				stmt.setNull(2, Types.INTEGER);
				stmt.setInt(3, 0);
				stmt.setInt(4, 0);
				stmt.setInt(5, 0);
				stmt.setFloat(6, 0);
				stmt.setFloat(7, 0);
				stmt.setFloat(8, 0);
				stmt.setInt(9, 0);
				stmt.setString(10, "DECOR");
				stmt.setByte(11, decor.getRoom());
				stmt.setInt(12, playerId);
				stmt.setInt(13, decor.getObjectId());
				stmt.setInt(14, decor.getTemplateId());
				stmt.addBatch();
			}

			stmt.executeBatch();
			con.commit();
		} catch (Exception e) {
			log.error("Failed to execute house decor update batch", e);
			return false;
		}
		return true;
	}

	private static boolean deleteObjects(Connection con, Collection<HouseObject<?>> objects) {
		if (objects.isEmpty())
			return true;

		try (PreparedStatement stmt = con.prepareStatement(DELETE_QUERY)) {
			for (HouseObject<?> obj : objects) {
				stmt.setInt(1, obj.getObjectId());
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

	private static boolean deleteDecors(Connection con, Collection<HouseDecoration> decors) {
		if (decors.isEmpty())
			return true;

		try (PreparedStatement stmt = con.prepareStatement(DELETE_QUERY)) {
			for (HouseDecoration decor : decors) {
				stmt.setInt(1, decor.getObjectId());
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

	public static boolean deletePlayerItems(int playerId) {
		log.info("Deleting player items");
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(CLEAN_PLAYER_QUERY)) {
			stmt.setInt(1, playerId);
			stmt.execute();
		} catch (Exception e) {
			log.error("Error in deleting all player registered items. PlayerObjId: " + playerId, e);
			return false;
		}
		return true;
	}

	public static void resetRegistry(int playerId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(RESET_QUERY)) {
			stmt.setInt(1, playerId);
			stmt.execute();
		} catch (Exception e) {
			log.error("Error in resetting player registered items. PlayerObjId: " + playerId, e);
		}
	}

}
