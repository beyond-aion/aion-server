package com.aionemu.loginserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.loginserver.model.base.BannedMacEntry;

/**
 * @author KID
 */
public class BannedMacDAO {

	public static Map<String, BannedMacEntry> load() {
		Map<String, BannedMacEntry> map = new HashMap<>();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("SELECT `address`,`time`,`details` FROM `banned_mac`")) {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String address = rs.getString("address");
				map.put(address, new BannedMacEntry(address, rs.getTimestamp("time"), rs.getString("details")));
			}
		} catch (SQLException e) {
			LoggerFactory.getLogger(BannedMacDAO.class).error("Error loading last saved server time", e);
		}
		return map;
	}

	public static boolean update(BannedMacEntry entry) {
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("REPLACE INTO `banned_mac` (`address`,`time`,`details`) VALUES (?,?,?)")) {
			ps.setString(1, entry.getMac());
			ps.setTimestamp(2, entry.getTime());
			ps.setString(3, entry.getDetails());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			LoggerFactory.getLogger(BannedMacDAO.class).error("Error storing BannedMacEntry " + entry.getMac(), e);
		}
		return false;
	}

	public static boolean remove(String address) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM `banned_mac` WHERE address=?")) {
			ps.setString(1, address);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			LoggerFactory.getLogger(BannedMacDAO.class).error("Error removing BannedMacEntry " + address, e);
		}
		return false;
	}

	public static void cleanExpiredBans() {
		DB.insertUpdate("DELETE FROM `banned_mac` WHERE time < current_date");
	}
}
