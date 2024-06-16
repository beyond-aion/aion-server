package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.PlayerLifeStats;

/**
 * @author Mr. Poke
 */
public class PlayerLifeStatsDAO {

	private static final Logger log = LoggerFactory.getLogger(PlayerLifeStatsDAO.class);
	public static final String INSERT_QUERY = "INSERT INTO `player_life_stats` (`player_id`, `hp`, `mp`, `fp`) VALUES (?,?,?,?)";
	public static final String SELECT_QUERY = "SELECT `hp`, `mp`, `fp` FROM `player_life_stats` WHERE `player_id`=?";
	public static final String UPDATE_QUERY = "UPDATE player_life_stats set `hp`=?, `mp`=?, `fp`=? WHERE `player_id`=?";

	public static void loadPlayerLifeStat(Player player) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, player.getObjectId());
			try (ResultSet rset = stmt.executeQuery()) {
				if (rset.next()) {
					PlayerLifeStats lifeStats = player.getLifeStats();
					lifeStats.setCurrentHp(rset.getInt("hp"));
					lifeStats.setCurrentMp(rset.getInt("mp"));
					lifeStats.setCurrentFp(rset.getInt("fp"));
				} else
					insertPlayerLifeStat(player);
			}
		} catch (Exception e) {
			log.error("Could not restore PlayerLifeStat data for playerObjId: " + player.getObjectId() + " from DB: " + e.getMessage(), e);
		}
	}

	public static void insertPlayerLifeStat(Player player) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
			stmt.setInt(1, player.getObjectId());
			stmt.setInt(2, player.getLifeStats().getCurrentHp());
			stmt.setInt(3, player.getLifeStats().getCurrentMp());
			stmt.setInt(4, player.getLifeStats().getCurrentFp());
			stmt.execute();
		} catch (Exception e) {
			log.error("Could not store PlayerLifeStat data for player " + player.getObjectId() + " from DB: " + e.getMessage(), e);
		}
	}

	public static void updatePlayerLifeStat(Player player) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY)) {
			stmt.setInt(1, player.getLifeStats().getCurrentHp());
			stmt.setInt(2, player.getLifeStats().getCurrentMp());
			stmt.setInt(3, player.getLifeStats().getCurrentFp());
			stmt.setInt(4, player.getObjectId());
			stmt.execute();
		} catch (Exception e) {
			log.error("Could not update PlayerLifeStat data for player " + player.getObjectId() + " from DB: " + e.getMessage(), e);
		}
	}
}
