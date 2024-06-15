package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.gameserver.model.gameobjects.player.MacroList;

/**
 * Macrosses DAO
 * <p/>
 * Created on: 13.07.2009 17:05:56
 * 
 * @author Aquanox
 */
public class PlayerMacrossesDAO {

	private static final Logger log = LoggerFactory.getLogger(PlayerMacrossesDAO.class);

	public static final String INSERT_QUERY = "INSERT INTO `player_macrosses` (`player_id`, `order`, `macro`) VALUES (?,?,?)";
	public static final String UPDATE_QUERY = "UPDATE `player_macrosses` SET `macro`=? WHERE `player_id`=? AND `order`=?";
	public static final String DELETE_QUERY = "DELETE FROM `player_macrosses` WHERE `player_id`=? AND `order`=?";
	public static final String SELECT_QUERY = "SELECT `order`, `macro` FROM `player_macrosses` WHERE `player_id`=?";

	public static void addMacro(final int playerId, final int macroPosition, final String macro) {
		DB.insertUpdate(INSERT_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, playerId);
				stmt.setInt(2, macroPosition);
				stmt.setString(3, macro);
				stmt.execute();
			}
		});
	}

	public static void updateMacro(final int playerId, final int macroPosition, final String macro) {
		DB.insertUpdate(UPDATE_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, macro);
				stmt.setInt(2, playerId);
				stmt.setInt(3, macroPosition);
				stmt.execute();
			}
		});
	}

	public static void deleteMacro(final int playerId, final int macroPosition) {
		DB.insertUpdate(DELETE_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, playerId);
				stmt.setInt(2, macroPosition);
				stmt.execute();
			}
		});
	}

	public static MacroList restoreMacrosses(final int playerId) {
		Map<Integer, String> macrosses = new HashMap<>();
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, playerId);
			try (ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					int order = rset.getInt("order");
					String text = rset.getString("macro");
					macrosses.put(order, text);
				}
			}
		} catch (Exception e) {
			log.error("Could not restore MacroList data for player " + playerId + " from DB: " + e.getMessage(), e);
		}
		return new MacroList(macrosses);
	}
}
