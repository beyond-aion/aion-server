package mysql5;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeMap;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.custom.instance.CustomInstanceRank;
import com.aionemu.gameserver.custom.instance.CustomInstanceService;
import com.aionemu.gameserver.dao.CustomInstanceDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;

/**
 * @author Jo
 */
public class MySQL5CustomInstanceDAO extends CustomInstanceDAO {

	private static final String SELECT_QUERY = "SELECT * FROM `custom_instance`";
	private static final String UPDATE_QUERY = "REPLACE INTO `custom_instance` (`player_id`, `rank`, `last_entry`) VALUES (?,?,?)";

	@Override
	public Map<Integer, CustomInstanceRank> loadPlayerRanks() {
		Map<Integer, CustomInstanceRank> loadedRanks = new TreeMap<>();
		DB.select(SELECT_QUERY, new ParamReadStH() {

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					int playerId = rset.getInt("player_id");
					if (!loadedRanks.containsKey(playerId)) {
						int rank = rset.getInt("rank");
						long lastEntry = rset.getTimestamp("last_entry").getTime();
						loadedRanks.put(playerId, new CustomInstanceRank(rank, lastEntry, PersistentState.UPDATED));
					}
				}
			}

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
			}
		});

		return loadedRanks;
	}

	@Override
	public void storePlayer(int playerId) {
		CustomInstanceRank rank = CustomInstanceService.getInstance().getPlayerRankObject(playerId);
		if (rank == null || rank.getPersistentState() != PersistentState.UPDATE_REQUIRED)
			return;

		boolean success = DB.insertUpdate(UPDATE_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, playerId);
				stmt.setInt(2, rank.getRank());
				stmt.setTimestamp(3, new Timestamp(rank.getLastEntry()));
				stmt.execute();
			}
		});

		if (success)
			rank.setPersistentState(PersistentState.UPDATED);
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}
}
