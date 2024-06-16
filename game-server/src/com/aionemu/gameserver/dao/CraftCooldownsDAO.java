package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author synchro2
 */
public class CraftCooldownsDAO {

	private static final Logger log = LoggerFactory.getLogger(CraftCooldownsDAO.class);

	public static final String INSERT_QUERY = "INSERT INTO `craft_cooldowns` (`player_id`, `delay_id`, `reuse_time`) VALUES (?,?,?)";
	public static final String DELETE_QUERY = "DELETE FROM `craft_cooldowns` WHERE `player_id`=?";
	public static final String SELECT_QUERY = "SELECT `delay_id`, `reuse_time` FROM `craft_cooldowns` WHERE `player_id`=?";

	public static void loadCraftCooldowns(Player player) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, player.getObjectId());
			try (ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					int delayId = rset.getInt("delay_id");
					long reuseTime = rset.getLong("reuse_time");
					player.getCraftCooldowns().put(delayId, reuseTime);
				}
			}
		} catch (SQLException e) {
			log.error("Couldn't load craft cooldowns for " + player, e);
		}
	}

	public static void storeCraftCooldowns(Player player) {
		deleteCraftCoolDowns(player);

		for (Map.Entry<Integer, Long> entry : player.getCraftCooldowns().entrySet()) {
			int delayId = entry.getKey();
			long reuseTime = entry.getValue();

			if (reuseTime < System.currentTimeMillis())
				continue;

			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
				stmt.setInt(1, player.getObjectId());
				stmt.setInt(2, delayId);
				stmt.setLong(3, reuseTime);
				stmt.execute();
			} catch (SQLException e) {
				log.error("Couldn't store craft cooldowns for " + player, e);
			}
		}
	}

	private static void deleteCraftCoolDowns(Player player) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DELETE_QUERY)) {
			stmt.setInt(1, player.getObjectId());
			stmt.execute();
		} catch (SQLException e) {
			log.error("Couldn't delete craft cooldowns for " + player, e);
		}
	}

}
