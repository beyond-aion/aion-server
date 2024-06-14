package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeRace;

/**
 * @author Sarynth
 */
public class SiegeDAO {

	private static final Logger log = LoggerFactory.getLogger(SiegeDAO.class);

	public static final String SELECT_QUERY = "SELECT `id`, `race`, `legion_id`, `occupy_count`, `faction_balance` FROM `siege_locations`";
	public static final String INSERT_QUERY = "INSERT INTO `siege_locations` (`id`, `race`, `legion_id`, `occupy_count`, `faction_balance`) VALUES(?, ?, ?, ?, ?)";
	public static final String UPDATE_QUERY = "UPDATE `siege_locations` SET  `race` = ?, `legion_id` = ?, `occupy_count` = ?, `faction_balance` = ? WHERE `id` = ?";

	public static boolean loadSiegeLocations(final Map<Integer, SiegeLocation> locations) {
		boolean success = true;
		List<Integer> loaded = new ArrayList<>();

		try {
			try (Connection con = DatabaseFactory.getConnection();
					 PreparedStatement stmt = con.prepareStatement(SELECT_QUERY);
					 ResultSet resultSet = stmt.executeQuery()) {
				while (resultSet.next()) {
					SiegeLocation loc = locations.get(resultSet.getInt("id"));
					loc.setRace(SiegeRace.valueOf(resultSet.getString("race")));
					loc.setLegionId(resultSet.getInt("legion_id"));
					loc.setOccupiedCount(resultSet.getInt("occupy_count"));
					loc.setFactionBalance(resultSet.getInt("faction_balance"));
					loaded.add(loc.getLocationId());
				}
			}
		} catch (Exception e) {
			log.warn("Error loading Siege informaiton from database: " + e.getMessage(), e);
			success = false;
		}

		for (Map.Entry<Integer, SiegeLocation> entry : locations.entrySet()) {
			SiegeLocation sLoc = entry.getValue();
			if (!loaded.contains(sLoc.getLocationId())) {
				insertSiegeLocation(sLoc);
			}
		}

		return success;
	}

	public static boolean updateSiegeLocation(final SiegeLocation siegeLocation) {
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = DatabaseFactory.getConnection();
			stmt = con.prepareStatement(UPDATE_QUERY);
			stmt.setString(1, siegeLocation.getRace().toString());
			stmt.setInt(2, siegeLocation.getLegionId());
			stmt.setInt(3, siegeLocation.getOccupiedCount());
			stmt.setInt(4, siegeLocation.getFactionBalance());
			stmt.setInt(5, siegeLocation.getLocationId());
			stmt.execute();
		} catch (Exception e) {
			log.error("Error update Siege Location: " + siegeLocation.getLocationId() + " to race: " + siegeLocation.getRace().toString(), e);
			return false;
		} finally {
			DatabaseFactory.close(stmt, con);
		}
		return true;
	}

	/**
	 * @param siegeLocation
	 * @return success
	 */
	private static boolean insertSiegeLocation(final SiegeLocation siegeLocation) {
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = DatabaseFactory.getConnection();
			stmt = con.prepareStatement(INSERT_QUERY);
			stmt.setInt(1, siegeLocation.getLocationId());
			stmt.setString(2, siegeLocation.getRace().toString());
			stmt.setInt(3, siegeLocation.getLegionId());
			stmt.setInt(4, siegeLocation.getOccupiedCount());
			stmt.setInt(5, siegeLocation.getFactionBalance());
			stmt.execute();
		} catch (Exception e) {
			log.error("Error insert Siege Location: " + siegeLocation.getLocationId(), e);
			return false;
		} finally {
			DatabaseFactory.close(stmt, con);

		}
		return true;
	}

	public static void updateLocation(final SiegeLocation siegeLocation) {
		updateSiegeLocation(siegeLocation);
	}

}
