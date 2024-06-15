package com.aionemu.gameserver.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.custom.instance.CustomInstanceRank;
import com.aionemu.gameserver.custom.instance.CustomInstanceRankedPlayer;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;

/**
 * @author Jo, Estrayl
 */
public class CustomInstanceDAO {

	private static final Logger log = LoggerFactory.getLogger(CustomInstanceDAO.class);

	private static final String SELECT_QUERY = "SELECT * FROM `custom_instance` WHERE ? = player_id";
	private static final String UPDATE_QUERY = "REPLACE INTO `custom_instance` (`player_id`, `rank`, `last_entry`, `max_rank`, `dps`) VALUES (?,?,?,?,?)";
	private static final String SELECT_TOP10_QUERY = "SELECT c.*, p.name, p.player_class FROM custom_instance c, players p WHERE c.player_id = p.id AND p.race = ? AND c.last_entry > NOW() - INTERVAL 14 DAY ORDER BY c.rank DESC, c.last_entry DESC LIMIT 10";

	public static CustomInstanceRank loadPlayerRankObject(int playerId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, playerId);
			ResultSet rset = stmt.executeQuery();
			if (rset.next())
				return new CustomInstanceRank(playerId, rset.getInt("rank"), rset.getTimestamp("last_entry").getTime(), rset.getInt("max_rank"),
					rset.getInt("dps"));
		} catch (SQLException e) {
			log.error("[CUSTOM_INSTANCE] Error loading rank object on player id " + playerId, e);
		}
		return null;
	}

	public static boolean storePlayer(CustomInstanceRank rankObj) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY)) {
			stmt.setInt(1, rankObj.getPlayerId());
			stmt.setInt(2, rankObj.getRank());
			stmt.setTimestamp(3, new Timestamp(rankObj.getLastEntry()));
			stmt.setInt(4, rankObj.getMaxRank());
			stmt.setInt(5, rankObj.getDps());
			stmt.execute();
			return true;
		} catch (SQLException e) {
			log.error("[CUSTOM_INSTANCE] Error storing last entries on player id " + rankObj.getPlayerId(), e);
			return false;
		}
	}

	public static List<CustomInstanceRankedPlayer> loadTop10(Race race) {
		List<CustomInstanceRankedPlayer> players = new ArrayList<>(10);
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_TOP10_QUERY)) {
			stmt.setString(1, race.toString());
			ResultSet rset = stmt.executeQuery();
			while (rset.next()) {
				int playerId = rset.getInt("c.player_id");
				int rank = rset.getInt("c.rank");
				long lastEntry = rset.getTimestamp("c.last_entry").getTime();
				int maxRank = rset.getInt("c.max_rank");
				int dps = rset.getInt("c.dps");
				String name = rset.getString("p.name");
				PlayerClass playerClass = PlayerClass.valueOf(rset.getString("p.player_class"));
				players.add(new CustomInstanceRankedPlayer(playerId, rank, lastEntry, maxRank, dps, name, playerClass));
			}
		} catch (SQLException e) {
			log.error("[CUSTOM_INSTANCE] Error loading top 10 " + race + " players", e);
		}
		return players;
	}

}
