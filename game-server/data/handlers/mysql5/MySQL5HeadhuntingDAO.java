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
import com.aionemu.gameserver.dao.HeadhuntingDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.model.event.Headhunter;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.services.PvpService;

/**
 * Created on 30.05.2016
 * 
 * @author Estrayl
 * @since AION 4.8
 */
public class MySQL5HeadhuntingDAO extends HeadhuntingDAO {

	private static final String SELECT_QUERY = "SELECT * FROM `headhunting`";
	private static final String UPDATE_QUERY = "REPLACE INTO `headhunting` (`hunter_id`, `accumulated_kills`, `last_update`) VALUES (?,?,?)";
	private static final String DELETE_QUERY = "DELETE FROM `headhunting`";

	@Override
	public Map<Integer, Headhunter> loadHeadhunters() {
		Map<Integer, Headhunter> loadedHunters = new TreeMap<>();
		DB.select(SELECT_QUERY, new ParamReadStH() {

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					int playerId = rset.getInt("hunter_id");
					if (!loadedHunters.containsKey(playerId)) {
						int accumulatedKills = rset.getInt("accumulated_kills");
						long lastUpdate = rset.getTimestamp("last_update").getTime();
						loadedHunters.put(playerId, new Headhunter(playerId, accumulatedKills, lastUpdate, PersistentState.UPDATED));
					}
				}
			}

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
			}
		});
		
		return loadedHunters;
	}
	
	@Override
	public boolean clearTables() {
		return DB.insertUpdate(DELETE_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.execute();
			}
		});
	}

	@Override
	public void storeHeadhunter(int hunterId) {
		Headhunter hunter = PvpService.getInstance().getHeadhunter(hunterId);
		if (hunter == null || hunter.getPersistentState() != PersistentState.UPDATE_REQUIRED)
			return;
			
		boolean success = DB.insertUpdate(UPDATE_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, hunter.getHunterId());
				stmt.setInt(2, hunter.getKills());
				stmt.setTimestamp(3, new Timestamp(hunter.getLastUpdate()));
				stmt.execute();
			}
		});
		
		if (success)
			hunter.setPersistentState(PersistentState.UPDATED);
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}
}
