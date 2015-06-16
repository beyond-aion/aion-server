package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.configs.main.RankingConfig;
import com.aionemu.gameserver.dao.AbyssRankDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.AbyssRankingResult;
import com.aionemu.gameserver.model.Gender;
import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.AbyssRank;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.stats.AbyssRankEnum;

/**
 * @author ATracer, Divinity, nrg
 */
public class MySQL5AbyssRankDAO extends AbyssRankDAO {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(MySQL5AbyssRankDAO.class);
	public static final String SELECT_QUERY = "SELECT daily_ap, weekly_ap, ap, daily_gp, weekly_gp, gp, rank, top_ranking, daily_kill, weekly_kill, all_kill, max_rank, last_kill, last_ap, last_gp, last_update FROM abyss_rank WHERE player_id = ?";
	public static final String INSERT_QUERY = "INSERT INTO abyss_rank (player_id, daily_ap, weekly_ap, ap, rank, top_ranking, daily_kill, weekly_kill, all_kill, max_rank, last_kill, last_ap, last_update, daily_gp, weekly_gp, gp, last_gp) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	public static final String UPDATE_QUERY = "UPDATE abyss_rank SET  daily_ap = ?, weekly_ap = ?, ap = ?, rank = ?, top_ranking = ?, daily_kill = ?, weekly_kill = ?, all_kill = ?, max_rank = ?, last_kill = ?, last_ap = ?, last_update = ?, daily_gp = ?, weekly_gp = ?, gp = ?, last_gp = ? WHERE player_id = ?";
	public static final String DECREASE_GP_DAILY = "UPDATE abyss_rank SET gp = gp - ? WHERE rank = ?";
	public static final String DECREASE_GP_QUERY = "UPDATE abyss_rank SET gp = gp - ? WHERE player_id = ?";
	public static final String INCREASE_GP_QUERY = "UPDATE abyss_rank SET gp = gp + ? WHERE player_id = ?";
	public static final String SELECT_PLAYERS_RANKING = "SELECT abyss_rank.rank, abyss_rank.ap, abyss_rank.gp, abyss_rank.old_rank_pos, abyss_rank.rank_pos, players.name, legions.name, players.id, players.title_id, players.player_class, players.gender, players.exp FROM abyss_rank INNER JOIN players ON abyss_rank.player_id = players.id LEFT JOIN legion_members ON legion_members.player_id = players.id LEFT JOIN legions ON legions.id = legion_members.legion_id WHERE players.race = ? AND abyss_rank.gp > 0 ORDER BY abyss_rank.gp DESC LIMIT 0, 300";
	public static final String SELECT_PLAYERS_RANKING_ACTIVE_ONLY = "SELECT abyss_rank.rank, abyss_rank.ap, abyss_rank.gp, abyss_rank.old_rank_pos, abyss_rank.rank_pos, players.name, legions.name, players.id, players.title_id, players.player_class, players.gender, players.exp FROM abyss_rank INNER JOIN players ON abyss_rank.player_id = players.id LEFT JOIN legion_members ON legion_members.player_id = players.id LEFT JOIN legions ON legions.id = legion_members.legion_id WHERE players.race = ? AND abyss_rank.gp > 0 AND UNIX_TIMESTAMP(CURDATE())-UNIX_TIMESTAMP(players.last_online) <= ? * 24 * 60 * 60 ORDER BY abyss_rank.gp DESC LIMIT 0, 300";
	public static final String SELECT_LEGIONS_RANKING = "SELECT legions.id, legions.name, legions.contribution_points, legions.level as lvl, legions.old_rank_pos, legions.rank_pos FROM legions,legion_members,players WHERE players.race = ? AND legion_members.rank = 'BRIGADE_GENERAL' AND legion_members.player_id = players.id AND legion_members.legion_id = legions.id AND legions.contribution_points > 0 GROUP BY id ORDER BY legions.contribution_points DESC LIMIT 0,50";
	public static final String SELECT_GP_PLAYER = "SELECT player_id, abyss_rank.gp, abyss_rank.ap FROM abyss_rank, players WHERE abyss_rank.player_id = players.id AND players.race = ? AND (abyss_rank.gp >= ? OR abyss_rank.rank >= ?) ORDER by gp DESC";
	public static final String SELECT_GP_PLAYER_ACTIVE_ONLY = "SELECT player_id, abyss_rank.gp, abyss_rank.ap FROM abyss_rank, players WHERE abyss_rank.player_id = players.id AND players.race = ? AND (abyss_rank.gp >= ? OR abyss_rank.rank >= ?) AND UNIX_TIMESTAMP(CURDATE())-UNIX_TIMESTAMP(players.last_online) <= ? * 24 * 60 * 60 ORDER BY gp DESC";
	public static final String UPDATE_RANK = "UPDATE abyss_rank SET  rank = ?, top_ranking = ? WHERE player_id = ?";
	public static final String SELECT_LEGION_COUNT = "SELECT COUNT(player_id) as players FROM legion_members WHERE legion_id = ?";
	public static final String UPDATE_PLAYER_RANK_LIST = "UPDATE abyss_rank SET abyss_rank.old_rank_pos = abyss_rank.rank_pos, abyss_rank.rank_pos = @a:=@a+1 where player_id in (SELECT id FROM players where race = ?) order by gp desc" + (RankingConfig.TOP_RANKING_SMALL_CACHE ? " limit 500" : "");  //only 300 positions are relevant later, so we update them + some extra positions that can get into the toprankings
	public static final String UPDATE_PLAYER_RANK_LIST_ACTIVE_ONLY = "UPDATE abyss_rank SET abyss_rank.old_rank_pos = abyss_rank.rank_pos, abyss_rank.rank_pos = @a:=@a+1 where player_id in (SELECT id FROM players where race = ? AND UNIX_TIMESTAMP(CURDATE())-UNIX_TIMESTAMP(players.last_online) <= ? * 24 * 60 * 60) order by gp desc" + (RankingConfig.TOP_RANKING_SMALL_CACHE ? " limit 500" : "");  //only 300 positions are relevant later, so we update them + some extra positions that can get into the toprankings
	public static final String UPDATE_LEGION_RANK_LIST = "UPDATE legions SET legions.old_rank_pos = legions.rank_pos, legions.rank_pos = @a:=@a+1 where id in (SELECT legion_id FROM legion_members, players where rank = 'BRIGADE_GENERAL' AND players.id = legion_members.player_id and players.race = ?) order by legions.contribution_points DESC" + (RankingConfig.TOP_RANKING_SMALL_CACHE ? " limit 75" : ""); //only 50 positions are relevant later, so we update them + some extra positions that can get into the toprankings

	@Override
	public AbyssRank loadAbyssRank(int playerId) {
		AbyssRank abyssRank = null;

		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
				stmt.setInt(1, playerId);
				try (ResultSet resultSet = stmt.executeQuery()) {
					if (resultSet.next()) {
						int daily_ap = resultSet.getInt("daily_ap");
						int weekly_ap = resultSet.getInt("weekly_ap");
						int ap = resultSet.getInt("ap");
						int rank = resultSet.getInt("rank");
						int top_ranking = resultSet.getInt("top_ranking");
						int daily_kill = resultSet.getInt("daily_kill");
						int weekly_kill = resultSet.getInt("weekly_kill");
						int all_kill = resultSet.getInt("all_kill");
						int max_rank = resultSet.getInt("max_rank");
						int last_kill = resultSet.getInt("last_kill");
						int last_ap = resultSet.getInt("last_ap");
						long last_update = resultSet.getLong("last_update");
						int daily_gp = resultSet.getInt("daily_gp");
						int weekly_gp = resultSet.getInt("weekly_gp");
						int gp = resultSet.getInt("gp");
						int last_gp = resultSet.getInt("last_gp");

						abyssRank = new AbyssRank(daily_ap, weekly_ap, ap, rank, top_ranking, daily_kill, weekly_kill, all_kill, max_rank, last_kill,
							last_ap, last_update, daily_gp,	weekly_gp, gp, last_gp);
						abyssRank.setPersistentState(PersistentState.UPDATED);
					}
					else {
						abyssRank = new AbyssRank(0, 0, 0, 1, 0, 0, 0, 0, 1, 0, 0, System.currentTimeMillis(), 0, 0, 0, 0);
						abyssRank.setPersistentState(PersistentState.NEW);
					}
				}
			}
		}
		catch (SQLException e) {
			log.error("loadAbyssRank", e);
		}
		return abyssRank;
	}

	@Override
	public void loadAbyssRank(final Player player) {
		AbyssRank rank = loadAbyssRank(player.getObjectId());
		player.setAbyssRank(rank);
	}

	@Override
	public boolean storeAbyssRank(Player player) {
		AbyssRank rank = player.getAbyssRank();
		boolean result = false;
		switch (rank.getPersistentState()) {
			case NEW:
				result = addRank(player.getObjectId(), rank);
				break;
			case UPDATE_REQUIRED:
				result = updateRank(player.getObjectId(), rank);
				break;
		}
		rank.setPersistentState(PersistentState.UPDATED);
		return result;
	}

	/**
	 * @param objectId
	 * @param rank
	 * @return
	 */
	private boolean addRank(final int objectId, final AbyssRank rank) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
				stmt.setInt(1, objectId);
				stmt.setInt(2, rank.getDailyAP());
				stmt.setInt(3, rank.getWeeklyAP());
				stmt.setInt(4, rank.getAp());
				stmt.setInt(5, rank.getRank().getId());
				stmt.setInt(6, rank.getTopRanking());
				stmt.setInt(7, rank.getDailyKill());
				stmt.setInt(8, rank.getWeeklyKill());
				stmt.setInt(9, rank.getAllKill());
				stmt.setInt(10, rank.getMaxRank());
				stmt.setInt(11, rank.getLastKill());
				stmt.setInt(12, rank.getLastAP());
				stmt.setLong(13, rank.getLastUpdate());
				stmt.setInt(14, rank.getDailyGP());
				stmt.setInt(15, rank.getWeeklyGP());
				stmt.setInt(16, rank.getCurrentGP());
				stmt.setInt(17, rank.getLastGP());
				stmt.execute();
				return true;
			}
		}
		catch (SQLException e) {
			log.error("addRank", e);
			return false;
		}
	}

	/**
	 * @param objectId
	 * @param rank
	 * @return
	 */
	private boolean updateRank(final int objectId, final AbyssRank rank) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY)) {
				stmt.setInt(1, rank.getDailyAP());
				stmt.setInt(2, rank.getWeeklyAP());
				stmt.setInt(3, rank.getAp());
				stmt.setInt(4, rank.getRank().getId());
				stmt.setInt(5, rank.getTopRanking());
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
				stmt.setInt(17, objectId);
				stmt.execute();
				return true;
			}
		}
		catch (SQLException e) {
			log.error("updateRank", e);
			return false;
		}
	}
	
	public void dailyUpdateGp(AbyssRankEnum rank) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DECREASE_GP_DAILY)) {
				stmt.setInt(1, rank.getGpLossPerDay());
				stmt.setInt(2, rank.getId());
				stmt.execute();
			}
		}
		catch (SQLException e) {
			log.error("dailyUpdateGp", e);
		}
	}
	
	public void increaseGp(int playerObjId, int additionalGp) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INCREASE_GP_QUERY)) {
				stmt.setInt(1, additionalGp);
				stmt.setInt(2, playerObjId);
				stmt.execute();
			}
		}
		catch (SQLException e) {
			log.error("increaseGp", e);
		}
	}
	
	public void decreaseGp(int playerObjId, int gpToRemove) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DECREASE_GP_QUERY)) {
				stmt.setInt(1, gpToRemove);
				stmt.setInt(2, playerObjId);
				stmt.execute();
			}
		}
		catch (SQLException e) {
			log.error("decreaseGp", e);
		}
	}

	@Override
	public ArrayList<AbyssRankingResult> getAbyssRankingPlayers(final Race race, final int maxOfflineDays) {
		final ArrayList<AbyssRankingResult> results = new ArrayList<AbyssRankingResult>();
		try {
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement stmt = con.prepareStatement(maxOfflineDays > 0 ? SELECT_PLAYERS_RANKING_ACTIVE_ONLY : SELECT_PLAYERS_RANKING)) {
				stmt.setString(1, race.toString());
				if (maxOfflineDays > 0) {
					stmt.setInt(2, maxOfflineDays);
				}

				try (ResultSet resultSet = stmt.executeQuery()) {
					while (resultSet.next()) {
						String name = resultSet.getString("players.name");
						int playerAbyssRank = resultSet.getInt("abyss_rank.rank");
						int ap = resultSet.getInt("abyss_rank.ap");
						int gp = resultSet.getInt("abyss_rank.gp");
						int playerTitle = resultSet.getInt("players.title_id");
						int playerId = resultSet.getInt("players.id");
						String playerClassStr = resultSet.getString("players.player_class");
						int playerLevel = DataManager.PLAYER_EXPERIENCE_TABLE.getLevelForExp(resultSet.getLong("players.exp"));
						String playerLegion = resultSet.getString("legions.name");
						int oldRankPos = resultSet.getInt("old_rank_pos");
						int rankPos = resultSet.getInt("rank_pos");
						PlayerClass playerClass = PlayerClass.getPlayerClassByString(playerClassStr);
						if (playerClass == null) {
							continue;
						}
						Gender gender = Gender.valueOf(resultSet.getString("players.gender"));
						AbyssRankingResult rsl = new AbyssRankingResult(name, playerAbyssRank, playerId, ap, gp, playerTitle, playerClass, gender, playerLevel,
							playerLegion, oldRankPos, rankPos);
						results.add(rsl);
					}
				}
			}
		}
		catch (SQLException e) {
			log.error("getAbyssRankingPlayers", e);
		}
		return results;
	}

	@Override
	public ArrayList<AbyssRankingResult> getAbyssRankingLegions(final Race race) {
		final ArrayList<AbyssRankingResult> results = new ArrayList<AbyssRankingResult>();
		DB.select(SELECT_LEGIONS_RANKING, new ParamReadStH() {

			@Override
			public void handleRead(ResultSet arg0) throws SQLException {
				while (arg0.next()) {
					String name = arg0.getString("legions.name");
					long cp = arg0.getLong("legions.contribution_points");
					int legionId = arg0.getInt("legions.id");
					int legionLevel = arg0.getInt("lvl");
					int legionMembers = getLegionMembersCount(legionId);
					int oldRankPos = arg0.getInt("old_rank_pos");
					int rankPos = arg0.getInt("rank_pos");
					AbyssRankingResult rsl = new AbyssRankingResult(cp, name, legionId, legionLevel, legionMembers, oldRankPos, rankPos);
					results.add(rsl);
				}
			}

			@Override
			public void setParams(PreparedStatement arg0) throws SQLException {
				arg0.setString(1, race.toString());
			}
		});
		return results;
	}

	private int getLegionMembersCount(final int legionId) {
		final int[] result = new int[1];
		DB.select(SELECT_LEGION_COUNT, new ParamReadStH() {

			@Override
			public void handleRead(ResultSet arg0) throws SQLException {
				while (arg0.next()) {
					result[0] += arg0.getInt("players");
				}
			}

			@Override
			public void setParams(PreparedStatement arg0) throws SQLException {
				arg0.setInt(1, legionId);
			}
		});
		return result[0];
	}

	@Override
	public Map<Integer, Integer[]> loadPlayersGpAp(final Race race, final AbyssRankEnum limitRank, final int maxOfflineDays) {
		final Map<Integer, Integer[]> results = new HashMap<>();
		DB.select(maxOfflineDays > 0 ? SELECT_GP_PLAYER_ACTIVE_ONLY : SELECT_GP_PLAYER, new ParamReadStH() {

			@Override
			public void handleRead(ResultSet rs) throws SQLException {
				while (rs.next()) {
					int playerId = rs.getInt("player_id");
					int gp = rs.getInt("gp");
					int ap = rs.getInt("ap");
					Integer[] result = {gp, ap};
					results.put(playerId, result);
				}
			}

			@Override
			public void setParams(PreparedStatement ps) throws SQLException {
				ps.setString(1, race.toString());
				ps.setInt(2, limitRank.getRequiredGP());
				ps.setInt(3, limitRank.getId());

				if (maxOfflineDays > 0) {
					ps.setInt(4, maxOfflineDays);
				}
			}
		});
		return results;
	}

	@Override
	public void updateAbyssRank(int playerId, AbyssRankEnum rankEnum) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_RANK)) {
				stmt.setInt(1, rankEnum.getId());
				stmt.setInt(2, rankEnum.getQuota());
				stmt.setInt(3, playerId);
				stmt.execute();
			}
		}
		catch (SQLException e) {
			log.error("updateAbyssRank", e);
		}
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}

	/*
	 * (non-Javadoc)
	 * @see com.aionemu.gameserver.dao.AbyssRankDAO#updateRankList()
	 */
	@Override
	public void updateRankList(final int maxOfflineDays) {
		try {
			try (Connection con = DatabaseFactory.getConnection()) {
				try (PreparedStatement stmt = con.prepareStatement(maxOfflineDays > 0 ? UPDATE_PLAYER_RANK_LIST_ACTIVE_ONLY
					: UPDATE_PLAYER_RANK_LIST)) {
					stmt.addBatch("SET @a:=0;");
					stmt.setString(1, "ELYOS");
					if (maxOfflineDays > 0) {
						stmt.setInt(2, maxOfflineDays);
					}
					stmt.addBatch();
					stmt.addBatch("SET @a:=0;");
					stmt.setString(1, "ASMODIANS");
					if (maxOfflineDays > 0) {
						stmt.setInt(2, maxOfflineDays);
					}
					stmt.addBatch();
					stmt.executeBatch();
				}
				try (PreparedStatement stmt = con.prepareStatement(UPDATE_LEGION_RANK_LIST)) {
					stmt.addBatch("SET @a:=0;");
					stmt.setString(1, "ELYOS");
					stmt.addBatch();
					stmt.addBatch("SET @a:=0;");
					stmt.setString(1, "ASMODIANS");
					stmt.addBatch();
					stmt.executeBatch();
				}
			}
		}
		catch (SQLException e) {
			log.error("updateRank", e);
		}
	}
}
