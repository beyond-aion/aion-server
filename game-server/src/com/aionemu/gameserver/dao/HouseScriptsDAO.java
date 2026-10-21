package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.gameobjects.player.PlayerScripts;

/**
 * @author Rolandas, Neon, Sykra
 */
public class HouseScriptsDAO {

	private static final Logger log = LoggerFactory.getLogger(HouseScriptsDAO.class);

	private static final String INSERT_QUERY = "INSERT INTO `house_scripts` (`house_id`,`script_id`,`script`,`uncompressed_size`) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE script=VALUES(script), uncompressed_size=VALUES(uncompressed_size)";
	private static final String DELETE_QUERY = "DELETE FROM `house_scripts` WHERE `house_id`=? AND `script_id`=?";
	private static final String DELETE_ALL_QUERY = "DELETE FROM `house_scripts` WHERE `house_id`=?";
	private static final String SELECT_QUERY = "SELECT `script_id`, `script`, `uncompressed_size` FROM `house_scripts` WHERE `house_id`=? ORDER BY `date_added`";

	public static void storeScript(int houseId, int scriptId, byte[] compressedXML, int uncompressedSize) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
			stmt.setInt(1, houseId);
			stmt.setInt(2, scriptId);
			stmt.setBytes(3, compressedXML);
			stmt.setInt(4, uncompressedSize);
			stmt.executeUpdate();
		} catch (Exception e) {
			log.error("Could not save script data for houseId: {}", houseId, e);
		}
	}

	public static PlayerScripts getPlayerScripts(int houseId) {
		PlayerScripts scripts = new PlayerScripts(houseId);
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, houseId);
			try (ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					scripts.set(rset.getInt("script_id"), rset.getBytes("script"), rset.getInt("uncompressed_size"), false);
				}
			}
		} catch (Exception e) {
			log.error("Could not restore script data for houseId: {}", houseId, e);
		}
		return scripts;
	}

	public static void deleteScript(int houseId, int scriptId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DELETE_QUERY)) {
			stmt.setInt(1, houseId);
			stmt.setInt(2, scriptId);
			stmt.executeUpdate();
		} catch (Exception e) {
			log.error("Could not delete script for houseId: {}", houseId, e);
		}
	}

	public static void deleteScriptsForHouse(int houseId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DELETE_ALL_QUERY)) {
			stmt.setInt(1, houseId);
			stmt.executeUpdate();
		} catch (Exception e) {
			log.error("Could not delete script for houseId: {}", houseId, e);
		}
	}

}
