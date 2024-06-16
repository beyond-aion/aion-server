package com.aionemu.gameserver.dao;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.town.Town;

/**
 * @author ViAl
 */
public class TownDAO {

	private static final Logger log = LoggerFactory.getLogger(TownDAO.class);

	private static final String SELECT_QUERY = "SELECT * FROM `towns` WHERE `race` = ?";
	private static final String INSERT_QUERY = "INSERT INTO `towns`(`id`,`level`,`points`, `race`) VALUES (?,?,?,?)";
	private static final String UPDATE_QUERY = "UPDATE `towns` SET `level` = ?, `points` = ?, `level_up_date` = ? WHERE `id` = ?";

	public static Map<Integer, Town> load(Race race) {
		Map<Integer, Town> towns = new HashMap<>();
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setString(1, race.toString());
			try (ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					int id = rset.getInt("id");
					int level = rset.getInt("level");
					int points = rset.getInt("points");
					Timestamp levelUpDate = rset.getTimestamp("level_up_date");
					Town town = new Town(id, level, points, race, levelUpDate);
					towns.put(town.getId(), town);
				}
			}
		} catch (SQLException e) {
			log.error("Could not load towns", e);
		}
		return towns;
	}

	public static void store(Town town) {
		switch (town.getPersistentState()) {
			case NEW -> insertTown(town);
			case UPDATE_REQUIRED -> updateTown(town);
		}
	}

	private static void insertTown(Town town) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
			stmt.setInt(1, town.getId());
			stmt.setInt(2, town.getLevel());
			stmt.setInt(3, town.getPoints());
			stmt.setString(4, town.getRace().toString());
			stmt.executeUpdate();
			town.setPersistentState(PersistentState.UPDATED);
		} catch (SQLException e) {
			log.error("Could not insert town " + town.getId(), e);
		}
	}

	private static void updateTown(Town town) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY)) {
			stmt.setInt(1, town.getLevel());
			stmt.setInt(2, town.getPoints());
			stmt.setTimestamp(3, town.getLevelUpDate());
			stmt.setInt(4, town.getId());
			stmt.executeUpdate();
			town.setPersistentState(PersistentState.UPDATED);
		} catch (SQLException e) {
			log.error("Could not update town " + town.getId(), e);
		}
	}

}
