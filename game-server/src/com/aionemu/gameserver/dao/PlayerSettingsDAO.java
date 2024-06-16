package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerSettings;

/**
 * @author ATracer, Neon
 */
public class PlayerSettingsDAO {

	private static final Logger log = LoggerFactory.getLogger(PlayerSettingsDAO.class);

	/**
	 * TODO 1) analyze possibility to zip settings 2) insert/update instead of replace 0 - uisettings 1 - shortcuts 2 - display 3 - deny
	 */
	public static PlayerSettings loadSettings(int playerId) {
		PlayerSettings playerSettings = new PlayerSettings();
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("SELECT * FROM player_settings WHERE player_id = ?")) {
			stmt.setInt(1, playerId);
			try (ResultSet resultSet = stmt.executeQuery()) {
				while (resultSet.next()) {
					int type = resultSet.getInt("settings_type");
					switch (type) {
						case 0 -> playerSettings.setUiSettings(resultSet.getBytes("settings"));
						case 1 -> playerSettings.setShortcuts(resultSet.getBytes("settings"));
						case 2 -> playerSettings.setHouseBuddies(resultSet.getBytes("settings"));
						case -1 -> playerSettings.setDisplay(resultSet.getInt("settings"));
						case -2 -> playerSettings.setDeny(resultSet.getInt("settings"));
					}
				}
			}
		} catch (Exception e) {
			log.error("Could not restore PlayerSettings data for player " + playerId + " from DB: " + e.getMessage(), e);
		}
		playerSettings.setPersistentState(PersistentState.UPDATED);
		return playerSettings;
	}

	public static void saveSettings(Player player) {
		int playerId = player.getObjectId();

		PlayerSettings playerSettings = player.getPlayerSettings();
		if (playerSettings.getPersistentState() == PersistentState.UPDATED)
			return;

		byte[] uiSettings = playerSettings.getUiSettings();
		byte[] shortcuts = playerSettings.getShortcuts();
		byte[] houseBuddies = playerSettings.getHouseBuddies();
		int display = playerSettings.getDisplay();
		int deny = playerSettings.getDeny();

		if (uiSettings != null) {
			DB.insertUpdate("REPLACE INTO player_settings values (?, ?, ?)", new IUStH() {

				@Override
				public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
					stmt.setInt(1, playerId);
					stmt.setInt(2, 0);
					stmt.setBytes(3, uiSettings);
					stmt.execute();
				}
			});
		}

		if (shortcuts != null) {
			DB.insertUpdate("REPLACE INTO player_settings values (?, ?, ?)", new IUStH() {

				@Override
				public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
					stmt.setInt(1, playerId);
					stmt.setInt(2, 1);
					stmt.setBytes(3, shortcuts);
					stmt.execute();
				}
			});
		}

		if (houseBuddies != null) {
			DB.insertUpdate("REPLACE INTO player_settings values (?, ?, ?)", new IUStH() {

				@Override
				public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
					stmt.setInt(1, playerId);
					stmt.setInt(2, 2);
					stmt.setBytes(3, houseBuddies);
					stmt.execute();
				}
			});
		}

		DB.insertUpdate("REPLACE INTO player_settings values (?, ?, ?)", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, playerId);
				stmt.setInt(2, -1);
				stmt.setInt(3, display);
				stmt.execute();
			}
		});

		DB.insertUpdate("REPLACE INTO player_settings values (?, ?, ?)", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, playerId);
				stmt.setInt(2, -2);
				stmt.setInt(3, deny);
				stmt.execute();
			}
		});

	}

}
