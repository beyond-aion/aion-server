package com.aionemu.gameserver.dao;

import java.sql.*;
import java.time.LocalDate;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Neon
 */
public class AdventDAO {

	public static boolean canReceiveReward(Player player, LocalDate date) {
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement("SELECT `last_day_received` FROM `advent` WHERE ? = account_id")) {
			stmt.setInt(1, player.getAccount().getId());
			ResultSet rs = stmt.executeQuery();
			if (!rs.next())
				return true;
			return rs.getDate("last_day_received").toLocalDate().isBefore(date);
		} catch (SQLException e) {
			return false;
		}
	}

	public static boolean storeLastReceivedDay(Player player, LocalDate date) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("REPLACE INTO `advent` VALUES (?, ?)")) {
			stmt.setInt(1, player.getAccount().getId());
			stmt.setDate(2, Date.valueOf(date));
			stmt.execute();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
}
