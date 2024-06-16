package com.aionemu.gameserver.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Map;
import java.util.TreeMap;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.model.event.Headhunter;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.services.PvpService;

/**
 * @author Estrayl
 */
public class HeadhuntingDAO {

	private static final String SELECT_QUERY = "SELECT * FROM `headhunting`";
	private static final String UPDATE_QUERY = "REPLACE INTO `headhunting` (`hunter_id`, `accumulated_kills`, `last_update`) VALUES (?,?,?)";
	private static final String DELETE_QUERY = "DELETE FROM `headhunting`";

	public static Map<Integer, Headhunter> loadHeadhunters() {
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

	public static boolean clearTables() {
		return DB.insertUpdate(DELETE_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.execute();
			}
		});
	}

	public static void storeHeadhunter(int hunterId) {
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

}
