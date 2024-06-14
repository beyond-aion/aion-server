package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;

/**
 * @author Ben, Neon
 */
public class ServerVariablesDAO {

	private static final Logger log = LoggerFactory.getLogger(ServerVariablesDAO.class);

	public static Integer loadInt(String var) {
		String value = load(var);
		return value == null ? null : Integer.parseInt(value);
	}

	public static Long loadLong(String var) {
		String value = load(var);
		return value == null ? null : Long.parseLong(value);
	}

	public static boolean store(String var, Object value) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement ps = con.prepareStatement("REPLACE INTO `server_variables` (`key`,`value`) VALUES (?,?)")) {
			ps.setString(1, var);
			ps.setString(2, value.toString());
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			log.error("Error storing " + value + " for variable " + var, e);
			return false;
		}
	}

	public boolean delete(String var) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement ps = con.prepareStatement("DELETE FROM `server_variables` WHERE `key`=?")) {
			ps.setString(1, var);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			log.error("Error loading value for " + var, e);
			return false;
		}
	}

	private static String load(String var) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement ps = con.prepareStatement("SELECT `value` FROM `server_variables` WHERE `key`=?")) {
			ps.setString(1, var);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return rs.getString("value");
		} catch (SQLException e) {
			log.error("Error loading value for " + var, e);
		}
		return null;
	}

}
