package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.utils.GenericValidator;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.PlayerRegisteredItemsDAO;
import com.aionemu.gameserver.model.gameobjects.HouseDecoration;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.house.HouseRegistry;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.model.templates.housing.PartType;
import com.aionemu.gameserver.services.item.HouseObjectFactory;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.world.World;

/**
 * @author Rolandas
 */
public class MySQL5PlayerRegisteredItemsDAO extends PlayerRegisteredItemsDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5PlayerRegisteredItemsDAO.class);

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

	@Override
	public int[] getUsedIDs() {
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

	@Override
	public void loadRegistry(HouseRegistry registry) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, registry.getOwner().getOwnerId());
			try (ResultSet rset = stmt.executeQuery()) {
				HashMap<PartType, List<HouseDecoration>> usedParts = new HashMap<>();
				while (rset.next()) {
					String area = rset.getString("area");
					if ("DECOR".equals(area)) {
						HouseDecoration dec = createDecoration(rset);
						if (!dec.getTemplate().getTags().contains(registry.getOwner().getBuilding().getPartsMatchTag()))
							continue;
						registry.putCustomPart(dec);
						if (dec.isUsed()) {
							if (registry.getOwner().getHouseType() != HouseType.PALACE && dec.getRoom() > 0)
								dec.setRoom(0);
							usedParts.computeIfAbsent(dec.getTemplate().getType(), k -> new ArrayList<>()).add(dec);
						}
						dec.setPersistentState(PersistentState.UPDATED);
					} else {
						HouseObject<?> obj = constructObject(registry, rset);
						registry.putObject(obj);
						obj.setPersistentState(PersistentState.UPDATED);
					}
				}
				for (PartType partType : PartType.values()) {
					if (usedParts.containsKey(partType)) {
						for (HouseDecoration usedDeco : usedParts.get(partType))
							registry.setPartInUse(usedDeco, usedDeco.getRoom());
						continue;
					}
					int roomCount = 1;
					if (registry.getOwner().getHouseType() == HouseType.PALACE && (partType == PartType.INFLOOR_ANY || partType == PartType.INWALL_ANY))
						roomCount = 6;
					for (int i = 0; i < roomCount; i++) {
						HouseDecoration def = registry.getDefaultPartByType(partType, i);
						if (def != null)
							registry.setPartInUse(def, i);
					}
				}
				registry.setPersistentState(PersistentState.UPDATED);
			}
		} catch (Exception e) {
			log.error("Could not load house registry data for player " + registry.getOwner().getOwnerId(), e);
		}
	}

	private HouseObject<?> constructObject(HouseRegistry registry, ResultSet rset) throws SQLException, IllegalAccessException {
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
				obj = HouseObjectFactory.createNew(registry.getOwner(), itemUniqueId, rset.getInt("item_id"));
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
		return obj;
	}

	private HouseDecoration createDecoration(ResultSet rset) throws SQLException {
		int itemUniqueId = rset.getInt("item_unique_id");
		int itemId = rset.getInt("item_Id");
		// Obsolete, rename it
		byte room = rset.getByte("room");
		HouseDecoration decor = new HouseDecoration(itemUniqueId, itemId, room);
		decor.setUsed(rset.getInt("owner_use_count") > 0);
		return decor;
	}

	@Override
	public boolean store(HouseRegistry registry, int playerId) {
		List<HouseObject<?>> objects = registry.getObjects();
		List<HouseDecoration> decors = registry.getAllParts();
		List<HouseObject<?>> objectsToAdd = objects.stream().filter(Persistable.NEW).collect(Collectors.toList());
		List<HouseObject<?>> objectsToUpdate = objects.stream().filter(Persistable.CHANGED).collect(Collectors.toList());
		List<HouseObject<?>> objectsToDelete = objects.stream().filter(Persistable.DELETED).collect(Collectors.toList());
		List<HouseDecoration> partsToAdd = decors.stream().filter(Persistable.NEW).collect(Collectors.toList());
		List<HouseDecoration> partsToUpdate = decors.stream().filter(Persistable.CHANGED).collect(Collectors.toList());
		List<HouseDecoration> partsToDelete = decors.stream().filter(Persistable.DELETED).collect(Collectors.toList());

		boolean objectDeleteResult = false;
		boolean partsDeleteResult = false;

		Connection con = null;
		try {
			con = DatabaseFactory.getConnection();
			con.setAutoCommit(false);
			objectDeleteResult = deleteObjects(con, objectsToDelete);
			partsDeleteResult = deleteParts(con, partsToDelete);
			storeObjects(con, objectsToUpdate, playerId, false);
			storeParts(con, partsToUpdate, playerId, false);
			storeObjects(con, objectsToAdd, playerId, true);
			storeParts(con, partsToAdd, playerId, true);
			registry.setPersistentState(PersistentState.UPDATED);
		} catch (SQLException e) {
			log.error("Can't save registered items for player: " + playerId, e);
		} finally {
			DatabaseFactory.close(con);
		}

		for (HouseObject<?> obj : objects) {
			if (obj.getPersistentState() == PersistentState.DELETED)
				registry.discardObject(obj.getObjectId());
			else
				obj.setPersistentState(PersistentState.UPDATED);
		}

		for (HouseDecoration decor : decors) {
			if (decor.getPersistentState() == PersistentState.DELETED)
				registry.discardPart(decor);
			else
				decor.setPersistentState(PersistentState.UPDATED);
		}

		if (objectDeleteResult)
			IDFactory.getInstance().releaseObjectIds(objectsToDelete);

		if (partsDeleteResult)
			IDFactory.getInstance().releaseObjectIds(partsToDelete);

		return true;
	}

	private boolean storeObjects(Connection con, Collection<HouseObject<?>> objects, int playerId, boolean isNew) {

		if (GenericValidator.isBlankOrNull(objects)) {
			return true;
		}

		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(isNew ? INSERT_QUERY : UPDATE_QUERY);

			for (HouseObject<?> obj : objects) {
				if (obj.getExpireTime() > 0)
					stmt.setInt(1, obj.getExpireTime());
				else
					stmt.setNull(1, Types.INTEGER);

				if (obj.getColor() == null)
					stmt.setNull(2, Types.INTEGER);
				else
					stmt.setInt(2, obj.getColor());

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
		} finally {
			DatabaseFactory.close(stmt);
		}
		return true;
	}

	private boolean storeParts(Connection con, Collection<HouseDecoration> parts, int playerId, boolean isNew) {

		if (GenericValidator.isBlankOrNull(parts)) {
			return true;
		}

		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(isNew ? INSERT_QUERY : UPDATE_QUERY);
			for (HouseDecoration part : parts) {
				stmt.setNull(1, Types.INTEGER);
				stmt.setNull(2, Types.INTEGER);
				stmt.setInt(3, 0);
				stmt.setInt(4, part.isUsed() ? 1 : 0);
				stmt.setInt(5, 0);
				stmt.setFloat(6, 0);
				stmt.setFloat(7, 0);
				stmt.setFloat(8, 0);
				stmt.setInt(9, 0);
				stmt.setString(10, "DECOR");
				stmt.setByte(11, part.getRoom());
				stmt.setInt(12, playerId);
				stmt.setInt(13, part.getObjectId());
				stmt.setInt(14, part.getTemplate().getId());
				stmt.addBatch();
			}

			stmt.executeBatch();
			con.commit();
		} catch (Exception e) {
			log.error("Failed to execute house parts update batch", e);
			return false;
		} finally {
			DatabaseFactory.close(stmt);
		}
		return true;
	}

	private boolean deleteObjects(Connection con, Collection<HouseObject<?>> objects) {
		if (GenericValidator.isBlankOrNull(objects)) {
			return true;
		}

		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(DELETE_QUERY);
			for (HouseObject<?> obj : objects) {
				stmt.setInt(1, obj.getObjectId());
				stmt.addBatch();
			}

			stmt.executeBatch();
			con.commit();
		} catch (Exception e) {
			log.error("Failed to execute delete batch", e);
			return false;
		} finally {
			DatabaseFactory.close(stmt);
		}
		return true;
	}

	private boolean deleteParts(Connection con, Collection<HouseDecoration> parts) {
		if (GenericValidator.isBlankOrNull(parts)) {
			return true;
		}

		PreparedStatement stmt = null;
		try {
			stmt = con.prepareStatement(DELETE_QUERY);
			for (HouseDecoration part : parts) {
				stmt.setInt(1, part.getObjectId());
				stmt.addBatch();
			}

			stmt.executeBatch();
			con.commit();
		} catch (Exception e) {
			log.error("Failed to execute delete batch", e);
			return false;
		} finally {
			DatabaseFactory.close(stmt);
		}
		return true;
	}

	@Override
	public boolean deletePlayerItems(int playerId) {
		log.info("Deleting player items");
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(CLEAN_PLAYER_QUERY)) {
				stmt.setInt(1, playerId);
				stmt.execute();
			}
		} catch (Exception e) {
			log.error("Error in deleting all player registered items. PlayerObjId: " + playerId, e);
			return false;
		}
		return true;
	}

	@Override
	public void resetRegistry(int playerId) {
		log.info("resetting player items: " + playerId);
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(RESET_QUERY)) {
				stmt.setInt(1, playerId);
				stmt.execute();
			}
		} catch (Exception e) {
			log.error("Error in resetting  player registered items. PlayerObjId: " + playerId, e);
		}
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}
}
