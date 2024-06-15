package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Neon
 */
public class VeteranRewardDAO {

	private static final Logger log = LoggerFactory.getLogger(VeteranRewardDAO.class);

	private static final String SELECT_QUERY = "SELECT `received_months` FROM `player_veteran_rewards` WHERE `player_id`=?";
	private static final String UPDATE_QUERY = "REPLACE INTO `player_veteran_rewards` (`player_id`, `received_months`) VALUES (?,?)";

	public static int loadReceivedMonths(Player player) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, player.getObjectId());
			ResultSet rset = stmt.executeQuery();
			if (rset.next())
				return rset.getInt("received_months");
			return 0;
		} catch (SQLException e) {
			log.error("Error loading received veteran reward months for player " + player, e);
			return -1;
		}
	}

	public static boolean storeReceivedMonths(Player player, int months) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY)) {
			stmt.setInt(1, player.getObjectId());
			stmt.setInt(2, months);
			stmt.execute();
			return true;
		} catch (Exception e) {
			log.error("Error saving received veteran reward months (" + months + ") for player " + player, e);
			return false;
		}
	}

}
