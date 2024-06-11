package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.AbyssRank;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * @author ATracer
 */
public class AbyssRankDAO {

	private static final Logger log = LoggerFactory.getLogger(AbyssRankDAO.class);

	private static final String SELECT_QUERY = "SELECT daily_ap, weekly_ap, ap, daily_gp, weekly_gp, gp, rank, daily_kill, weekly_kill, all_kill, max_rank, last_kill, last_ap, last_gp, last_update FROM abyss_rank WHERE player_id = ?";
	private static final String INSERT_QUERY = "INSERT INTO abyss_rank (player_id, daily_ap, weekly_ap, ap, rank, daily_kill, weekly_kill, all_kill, max_rank, last_kill, last_ap, last_update, daily_gp, weekly_gp, gp, last_gp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_QUERY = "UPDATE abyss_rank SET  daily_ap = ?, weekly_ap = ?, ap = ?, rank = ?, daily_kill = ?, weekly_kill = ?, all_kill = ?, max_rank = ?, last_kill = ?, last_ap = ?, last_update = ?, daily_gp = ?, weekly_gp = ?, gp = ?, last_gp = ? WHERE player_id = ?";
	private static final String DECREASE_GP_DAILY = "UPDATE abyss_rank SET gp = gp - ? WHERE rank = ?";
	private static final String DECREASE_GP_QUERY = "UPDATE abyss_rank SET gp = gp - ? WHERE player_id = ?";
	private static final String INCREASE_GP_QUERY = "UPDATE abyss_rank SET gp = gp + ? WHERE player_id = ?";
	private static final String INCREASE_GP_QUERY_WITH_STATS = "UPDATE abyss_rank SET gp = gp + ?, daily_gp = daily_gp + ?, weekly_gp = weekly_gp + ? WHERE player_id = ?";
	private static final String UPDATE_RANK = "UPDATE abyss_rank SET rank = ? WHERE player_id = ?";
	private static final String SELECT_RANKING_LIST_PLAYERS = "SELECT a.rank_pos, a.old_rank_pos, p.id, p.name, p.race, p.exp, a.rank, a.ap, a.gp, p.title_id, p.player_class, p.gender, l.name FROM abyss_rank a JOIN players p ON a.player_id = p.id LEFT JOIN legion_members lm ON lm.player_id = p.id LEFT JOIN legions l ON l.id = lm.legion_id WHERE a.rank_pos > 0";
	private static final String SELECT_RANKING_LIST_LEGIONS = "SELECT l.rank_pos, l.old_rank_pos, l.id, l.name, p.race, l.level, l.contribution_points FROM legions l, legion_members lm, players p WHERE lm.rank = 'BRIGADE_GENERAL' AND lm.player_id = p.id AND lm.legion_id = l.id AND l.rank_pos > 0 GROUP BY id";
	private static final String SELECT_RANKING_LIST_PLAYERS_GP = "SELECT a.rank_pos, a.player_id, a.gp FROM abyss_rank a, players p WHERE a.player_id = p.id AND p.race = ? AND a.rank_pos > 0 ORDER by a.rank_pos";
	private static final String SELECT_UNRANKED_PLAYERS_AP = "SELECT a.player_id, a.ap FROM abyss_rank a, players p WHERE a.player_id = p.id AND p.race = ? AND a.rank_pos = 0 AND a.rank >= ?";
	private static final String SELECT_LEGION_COUNT = "SELECT COUNT(player_id) as players FROM legion_members WHERE legion_id = ?";
	private static final String RESET_RANKING_LIST_PLAYERS = "UPDATE abyss_rank a SET a.old_rank_pos = a.rank_pos, a.rank_pos = 0 WHERE a.rank_pos > 0";
	private static final String RESET_RANKING_LIST_LEGIONS = "UPDATE legions l SET l.old_rank_pos = l.rank_pos, l.rank_pos = 0 WHERE l.rank_pos > 0";
	private static final String UPDATE_RANKING_LIST_PLAYERS_POSITIONS = "UPDATE abyss_rank SET rank_pos = @a:=@a+1 WHERE gp > 0 AND player_id IN (SELECT id FROM players WHERE race = ? AND (@minLastOnline IS NULL OR last_online >= @minLastOnline)) ORDER BY gp DESC LIMIT ?";
	private static final String UPDATE_RANKING_LIST_LEGIONS_POSITIONS = "UPDATE legions SET rank_pos = @a:=@a+1 WHERE id IN (SELECT legion_id FROM legion_members lm, players WHERE rank = 'BRIGADE_GENERAL' AND players.id = lm.player_id and players.race = ?) ORDER BY contribution_points DESC LIMIT ?";

	public static AbyssRank loadAbyssRank(int playerId) {
		AbyssRank abyssRank = null;
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, playerId);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				int daily_ap = rs.getInt("daily_ap");
				int weekly_ap = rs.getInt("weekly_ap");
				int ap = rs.getInt("ap");
				int rank = rs.getInt("rank");
				int daily_kill = rs.getInt("daily_kill");
				int weekly_kill = rs.getInt("weekly_kill");
				int all_kill = rs.getInt("all_kill");
				int max_rank = rs.getInt("max_rank");
				int last_kill = rs.getInt("last_kill");
				int last_ap = rs.getInt("last_ap");
				long last_update = rs.getLong("last_update");
				int daily_gp = rs.getInt("daily_gp");
				int weekly_gp = rs.getInt("weekly_gp");
				int gp = rs.getInt("gp");
				int last_gp = rs.getInt("last_gp");

				abyssRank = new AbyssRank(daily_ap, weekly_ap, ap, rank, daily_kill, weekly_kill, all_kill, max_rank, last_kill, last_ap, last_update,
					daily_gp, weekly_gp, gp, last_gp);
				abyssRank.setPersistentState(PersistentState.UPDATED);
			} else {
				abyssRank = new AbyssRank(0, 0, 0, 1, 0, 0, 0, 1, 0, 0, System.currentTimeMillis(), 0, 0, 0, 0);
				abyssRank.setPersistentState(PersistentState.NEW);
			}
		} catch (SQLException e) {
			log.error("Couldn't load abyss rank for player " + playerId, e);
		}
		return abyssRank;
	}

	public static void loadAbyssRank(Player player) {
		AbyssRank rank = loadAbyssRank(player.getObjectId());
		player.setAbyssRank(rank);
	}

	public static boolean storeAbyssRank(Player player) {
		AbyssRank rank = player.getAbyssRank();
		boolean result = false;
		switch (rank.getPersistentState()) {
			case NEW:
				result = insertRank(player.getObjectId(), rank);
				break;
			case UPDATE_REQUIRED:
				result = updateRank(player.getObjectId(), rank);
				break;
		}
		rank.setPersistentState(PersistentState.UPDATED);
		return result;
	}

	private static boolean insertRank(int playerId, AbyssRank rank) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
			stmt.setInt(1, playerId);
			stmt.setInt(2, rank.getDailyAP());
			stmt.setInt(3, rank.getWeeklyAP());
			stmt.setInt(4, rank.getAp());
			stmt.setInt(5, rank.getRank().getId());
			stmt.setInt(6, rank.getDailyKill());
			stmt.setInt(7, rank.getWeeklyKill());
			stmt.setInt(8, rank.getAllKill());
			stmt.setInt(9, rank.getMaxRank());
			stmt.setInt(10, rank.getLastKill());
			stmt.setInt(11, rank.getLastAP());
			stmt.setLong(12, rank.getLastUpdate());
			stmt.setInt(13, rank.getDailyGP());
			stmt.setInt(14, rank.getWeeklyGP());
			stmt.setInt(15, rank.getCurrentGP());
			stmt.setInt(16, rank.getLastGP());
			stmt.execute();
			return true;
		} catch (SQLException e) {
			log.error("Couldn't insert abyss rank for player " + playerId, e);
			return false;
		}
	}

	private static boolean updateRank(int playerId, AbyssRank rank) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY)) {
			stmt.setInt(1, rank.getDailyAP());
			stmt.setInt(2, rank.getWeeklyAP());
			stmt.setInt(3, rank.getAp());
			stmt.setInt(4, rank.getRank().getId());
			stmt.setInt(5, rank.getDailyKill());
			stmt.setInt(6, rank.getWeeklyKill());
			stmt.setInt(7, rank.getAllKill());
			stmt.setInt(8, rank.getMaxRank());
			stmt.setInt(9, rank.getLastKill());
			stmt.setInt(10, rank.getLastAP());
			stmt.setLong(11, rank.getLastUpdate());
			stmt.setInt(12, rank.getDailyGP());
			stmt.setInt(13, rank.getWeeklyGP());
			stmt.setInt(14, rank.getCurrentGP());
			stmt.setInt(15, rank.getLastGP());
			stmt.setInt(16, playerId);
			stmt.execute();
			return true;
		} catch (SQLException e) {
			log.error("Couldn't update abyss rank of player " + playerId, e);
			return false;
		}
	}

	public static void dailyUpdateGp(AbyssRankEnum rank) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement(DECREASE_GP_DAILY)) {
			stmt.setInt(1, rank.getGpLossPerDay());
			stmt.setInt(2, rank.getId());
			stmt.execute();
		} catch (SQLException e) {
			log.error("Couldn't decrease daily GP for rank " + rank, e);
		}
	}

	public static void increaseGp(int playerObjId, int additionalGp, boolean modifyStats) {
		String updateQuery = modifyStats ? INCREASE_GP_QUERY_WITH_STATS : INCREASE_GP_QUERY;
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement(updateQuery)) {
			if (modifyStats) {
				stmt.setInt(1, additionalGp);
				stmt.setInt(2, additionalGp);
				stmt.setInt(3, additionalGp);
				stmt.setInt(4, playerObjId);
			} else {
				stmt.setInt(1, additionalGp);
				stmt.setInt(2, playerObjId);
			}
			stmt.execute();
		} catch (SQLException e) {
			log.error("Couldn't increase {} GP for player {}", additionalGp, playerObjId, e);
		}
	}

	public static void decreaseGp(int playerObjId, int gpToRemove) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement(DECREASE_GP_QUERY)) {
			stmt.setInt(1, gpToRemove);
			stmt.setInt(2, playerObjId);
			stmt.execute();
		} catch (SQLException e) {
			log.error("Couldn't decrease {} GP from player {}", gpToRemove, playerObjId, e);
		}
	}

	public static List<RankingListPlayer> loadRankingListPlayers() {
		List<RankingListPlayer> results = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement(SELECT_RANKING_LIST_PLAYERS)) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int position = rs.getInt("a.rank_pos");
				int oldPosition = rs.getInt("a.old_rank_pos");
				int id = rs.getInt("p.id");
				String name = rs.getString("p.name");
				Race race = Race.valueOf(rs.getString("p.race"));
				int level = DataManager.PLAYER_EXPERIENCE_TABLE.getLevelForExp(rs.getLong("p.exp"));
				int rank = rs.getInt("a.rank");
				int ap = rs.getInt("a.ap");
				int gp = rs.getInt("a.gp");
				int title = rs.getInt("p.title_id");
				PlayerClass playerClass = PlayerClass.valueOf(rs.getString("p.player_class"));
				Gender gender = Gender.valueOf(rs.getString("p.gender"));
				String legionName = rs.getString("l.name");
				results.add(new RankingListPlayer(position, oldPosition, id, name, race, level, rank, ap, gp, title, playerClass, gender, legionName));
			}
		} catch (SQLException e) {
			log.error(null, e);
		}
		return results;
	}

	public static List<RankingListLegion> loadRankingListLegions() {
		List<RankingListLegion> results = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement(SELECT_RANKING_LIST_LEGIONS)) {
			ResultSet rs = stmt.executeQuery();
			while (rs.next()) {
				int position = rs.getInt("l.rank_pos");
				int oldPosition = rs.getInt("l.old_rank_pos");
				int id = rs.getInt("l.id");
				String name = rs.getString("l.name");
				Race race = Race.valueOf(rs.getString("p.race"));
				int level = rs.getInt("l.level");
				long contributionPoints = rs.getLong("l.contribution_points");
				int memberCount = loadLegionMemberCount(con, id);
				results.add(new RankingListLegion(position, oldPosition, id, name, race, level, contributionPoints, memberCount));
			}
		} catch (SQLException e) {
			log.error(null, e);
		}
		return results;
	}

	private static int loadLegionMemberCount(Connection con, int legionId) {
		try (PreparedStatement stmt = con.prepareStatement(SELECT_LEGION_COUNT)) {
			stmt.setInt(1, legionId);
			ResultSet rs = stmt.executeQuery();
			rs.next();
			return rs.getInt("players");
		} catch (SQLException e) {
			log.error("Couldn't load legion member count for legion " + legionId, e);
			return 0;
		}
	}

	public static List<RankingListPlayerGp> loadRankingListPlayersGp(Race race) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement(SELECT_RANKING_LIST_PLAYERS_GP)) {
			stmt.setString(1, race.toString());
			ResultSet rs = stmt.executeQuery();
			List<RankingListPlayerGp> rankingList = new ArrayList<>();
			while (rs.next()) {
				int rankPos = rs.getInt("rank_pos");
				int playerId = rs.getInt("player_id");
				int gp = rs.getInt("gp");
				rankingList.add(new RankingListPlayerGp(rankPos, playerId, gp));
			}
			return rankingList;
		} catch (SQLException e) {
			log.error("Couldn't load top ranks for race " + race, e);
			return null;
		}
	}

	public static Map<Integer, Integer> loadApOfPlayersNotInRankingList(Race race, AbyssRankEnum minRank) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement(SELECT_UNRANKED_PLAYERS_AP)) {
			stmt.setString(1, race.toString());
			stmt.setInt(2, minRank.getId());
			ResultSet rs = stmt.executeQuery();
			Map<Integer, Integer> apByPlayerId = new HashMap<>();
			while (rs.next())
				apByPlayerId.put(rs.getInt("player_id"), rs.getInt("ap"));
			return apByPlayerId;
		} catch (SQLException e) {
			log.error("Couldn't load ranks for race " + race + " (minRank " + minRank + ")", e);
			return null;
		}
	}

	public static void updateAbyssRank(int playerId, AbyssRankEnum rank) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement(UPDATE_RANK)) {
			stmt.setInt(1, rank.getId());
			stmt.setInt(2, playerId);
			stmt.execute();
		} catch (SQLException e) {
			log.error("Couldn't update abyss rank of player " + playerId + " to " + rank, e);
		}
	}

	public static void updateRankingLists(int maxOfflineDays, int playerLimit, int legionLimit) {
		try (Connection con = DatabaseFactory.getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement(RESET_RANKING_LIST_PLAYERS)) {
				stmt.executeUpdate();
			}
			try (PreparedStatement stmt = con.prepareStatement(UPDATE_RANKING_LIST_PLAYERS_POSITIONS)) {
				if (maxOfflineDays > 0)
					stmt.addBatch("SET @minLastOnline = CURDATE() - INTERVAL " + maxOfflineDays + " DAY;");
				stmt.addBatch("SET @a = 0;");
				stmt.setString(1, "ELYOS");
				stmt.setInt(2, playerLimit);
				stmt.addBatch();
				stmt.addBatch("SET @a = 0;");
				stmt.setString(1, "ASMODIANS");
				stmt.setInt(2, playerLimit);
				stmt.addBatch();
				if (maxOfflineDays > 0)
					stmt.addBatch("SET @minLastOnline = NULL;");
				stmt.executeBatch();
			}
			try (PreparedStatement stmt = con.prepareStatement(RESET_RANKING_LIST_LEGIONS)) {
				stmt.executeUpdate();
			}
			try (PreparedStatement stmt = con.prepareStatement(UPDATE_RANKING_LIST_LEGIONS_POSITIONS)) {
				stmt.addBatch("SET @a = 0;");
				stmt.setString(1, "ELYOS");
				stmt.setInt(2, legionLimit);
				stmt.addBatch();
				stmt.addBatch("SET @a = 0;");
				stmt.setString(1, "ASMODIANS");
				stmt.setInt(2, legionLimit);
				stmt.addBatch();
				stmt.executeBatch();
			}
		} catch (SQLException e) {
			log.error(null, e);
		}
	}

	public record RankingListPlayerGp(int position, int playerId, int gp) {}

	public record RankingListPlayer(int position, int oldPosition, int id, String name, Race race, int level, int abyssRank, int ap, int gp, int title,
																	PlayerClass playerClass, Gender gender, String legionName) {}

	public record RankingListLegion(int position, int oldPosition, int id, String name, Race race, int level, long contributionPoints,
																	int memberCount) {}

}
