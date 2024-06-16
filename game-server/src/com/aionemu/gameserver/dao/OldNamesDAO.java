package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
/**
 * @author synchro2
 */
public class OldNamesDAO {

	private static final Logger log = LoggerFactory.getLogger(OldNamesDAO.class);

	public static boolean isNameReserved(String oldName, String newName, int nameReservationDurationDays) {
		if (nameReservationDurationDays > 0) {
			try (Connection con = DatabaseFactory.getConnection();
					 PreparedStatement s = con.prepareStatement("SELECT COUNT(*) cnt FROM old_names WHERE old_name = ? AND COALESCE(new_name != ?, TRUE) AND renamed_date > NOW() - INTERVAL ? DAY")) {
				s.setString(1, newName);
				s.setString(2, oldName);
				s.setInt(3, nameReservationDurationDays);
				ResultSet rs = s.executeQuery();
				rs.next();
				return rs.getInt("cnt") > 0;
			} catch (SQLException e) {
				log.error("Couldn't check if name {} is reserved", newName, e);
			}
		}
		return false;
	}

	public static void insertNames(int playerId, String oldName, String newName) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("INSERT INTO `old_names` (`player_id`, `old_name`, `new_name`) VALUES (?, ?, ?)")) {
			stmt.setInt(1, playerId);
			stmt.setString(2, oldName);
			stmt.setString(3, newName);
			stmt.execute();
		} catch (SQLException e) {
			log.error("Could not insert names for player {}: {}>{}", playerId, oldName, newName, e);
		}
	}

}
