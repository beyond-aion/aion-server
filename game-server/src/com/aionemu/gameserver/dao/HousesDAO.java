package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.Building;
import com.aionemu.gameserver.model.templates.housing.BuildingType;
import com.aionemu.gameserver.model.templates.housing.HouseAddress;
import com.aionemu.gameserver.model.templates.housing.HousingLand;

/**
 * @author Rolandas
 */
public class HousesDAO {

	private static final Logger log = LoggerFactory.getLogger(HousesDAO.class);

	private static final String SELECT_HOUSES_QUERY = "SELECT * FROM houses WHERE address <> 2001 AND address <> 3001";
	private static final String SELECT_STUDIOS_QUERY = "SELECT * FROM houses WHERE address = 2001 OR address = 3001";

	private static final String ADD_HOUSE_QUERY = "INSERT INTO houses (id, address, building_id, player_id, acquire_time, settings, next_pay, sign_notice) "
		+ " VALUES (?,?,?,?,?,?,?,?)";

	private static final String UPDATE_HOUSE_QUERY = "UPDATE houses SET building_id=?, player_id=?, acquire_time=?, settings=?, next_pay=?, sign_notice=? WHERE id=?";
	private static final String DELETE_HOUSE_QUERY = "DELETE FROM houses WHERE player_id=?";

	public static int[] getUsedIDs() {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("SELECT DISTINCT id FROM houses", ResultSet.TYPE_SCROLL_INSENSITIVE,
					 ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = stmt.executeQuery();
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			int[] ids = new int[count];
			for (int i = 0; rs.next(); i++)
				ids[i] = rs.getInt("id");
			return ids;
		} catch (SQLException e) {
			log.error("Can't get list of IDs from houses table", e);
			return null;
		}
	}

	public static void storeHouse(House house) {
		if (house.getPersistentState() == PersistentState.NEW)
			insertNewHouse(house);
		else if (house.getPersistentState() == PersistentState.UPDATE_REQUIRED)
			updateHouse(house);
	}

	private static void insertNewHouse(House house) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(ADD_HOUSE_QUERY)) {
			stmt.setInt(1, house.getObjectId());
			stmt.setInt(2, house.getAddress().getId());
			stmt.setInt(3, house.getBuilding().getId());
			stmt.setInt(4, house.getOwnerId());
			stmt.setTimestamp(5, house.getAcquiredTime());
			stmt.setInt(6, house.getPermissionsForDB());
			stmt.setTimestamp(7, house.getNextPay());
			stmt.setString(8, house.getSignNotice());

			stmt.execute();
			house.setPersistentState(PersistentState.UPDATED);
		} catch (Exception e) {
			log.error("Could not insert house " + house.getObjectId(), e);
		}
	}

	private static void updateHouse(House house) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_HOUSE_QUERY)) {
			stmt.setInt(1, house.getBuilding().getId());
			stmt.setInt(2, house.getOwnerId());
			stmt.setTimestamp(3, house.getAcquiredTime());
			stmt.setInt(4, house.getPermissionsForDB());
			stmt.setTimestamp(5, house.getNextPay());
			stmt.setString(6, house.getSignNotice());

			stmt.setInt(7, house.getObjectId());
			stmt.execute();
			house.setPersistentState(PersistentState.UPDATED);
		} catch (Exception e) {
			log.error("Could not store house " + house.getObjectId(), e);
		}
	}

	public static Map<Integer, House> loadHouses(Collection<HousingLand> lands, boolean studios) {
		Map<Integer, House> houses = new HashMap<>();
		Map<Integer, HouseAddress> addressesById = new HashMap<>();
		Map<Integer, List<Building>> buildingsForAddress = new HashMap<>();
		for (HousingLand land : lands) {
			for (HouseAddress address : land.getAddresses()) {
				addressesById.put(address.getId(), address);
				buildingsForAddress.put(address.getId(), land.getBuildings());
			}
		}

		HashMap<Integer, Integer> addressHouseIds = new HashMap<>();

		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement(studios ? SELECT_STUDIOS_QUERY : SELECT_HOUSES_QUERY)) {
			try (ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					int houseId = rset.getInt("id");
					int buildingId = rset.getInt("building_id");
					HouseAddress address = addressesById.get(rset.getInt("address"));
					Building building = null;
					for (Building b : buildingsForAddress.get(address.getId())) {
						if (b.getId() == buildingId) {
							building = b;
							break;
						}
					}

					House house;
					if (building == null) {
						log.warn("Missing building type for address " + address.getId());
						continue;
					} else if (addressHouseIds.containsKey(address.getId())) {
						log.warn("Duplicate house address " + address.getId() + "!");
						continue;
					} else {
						house = new House(houseId, building, address, 0);
						if (building.getType() == BuildingType.PERSONAL_FIELD)
							addressHouseIds.put(address.getId(), houseId);
					}

					house.setOwnerId(rset.getInt("player_id"));
					house.setAcquiredTime(rset.getTimestamp("acquire_time"));
					house.setPermissionsFromDB(rset.getInt("settings"));
					house.setNextPay(rset.getTimestamp("next_pay"));
					house.setSignNotice(rset.getString("sign_notice"));
					house.setPersistentState(PersistentState.UPDATED);

					int id = studios ? house.getOwnerId() : address.getId();
					houses.put(id, house);
				}
			}
		} catch (Exception e) {
			log.error("Could not load houses", e);
		}
		return houses;
	}

	public static void deleteHouse(int playerId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DELETE_HOUSE_QUERY)) {
			stmt.setInt(1, playerId);
			stmt.execute();
		} catch (SQLException e) {
			log.error("Delete House failed", e);
		}
	}

}
