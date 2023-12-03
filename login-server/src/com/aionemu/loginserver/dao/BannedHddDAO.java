package com.aionemu.loginserver.dao;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;

/**
 * @author ViAl
 */
public class BannedHddDAO {

	public static boolean update(String serial, Timestamp time) {
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("REPLACE INTO `banned_hdd` (`serial`,`time`) VALUES (?,?)")) {
			ps.setString(1, serial);
			ps.setTimestamp(2, time);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			LoggerFactory.getLogger(BannedHddDAO.class).error("Error storing hdd serial ban " + serial, e);
		}
		return false;
	}

	public static boolean remove(String serial) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM `banned_hdd` WHERE serial=?")) {
			ps.setString(1, serial);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			LoggerFactory.getLogger(BannedHddDAO.class).error("Error removing hdd serial " + serial, e);
		}
		return false;
	}

	public static Map<String, Timestamp> load() {
		Map<String, Timestamp> map = new HashMap<>();
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM `banned_hdd`")) {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String serial = rs.getString("serial");
				Timestamp time = rs.getTimestamp("time");
				map.put(serial, time);
			}
		} catch (SQLException e) {
			LoggerFactory.getLogger(BannedHddDAO.class).error("Error loading last saved server time", e);
		}
		return map;
	}

	public static void cleanExpiredBans() {
		DB.insertUpdate("DELETE FROM `banned_hdd` WHERE time < current_date");
	}
}
