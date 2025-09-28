package com.aionemu.gameserver.dao;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.gameobjects.player.PlayerScripts;
import com.aionemu.gameserver.utils.xml.CompressUtil;

/**
 * @author Rolandas, Neon, Sykra
 */
public class HouseScriptsDAO {

	private static final Logger log = LoggerFactory.getLogger(HouseScriptsDAO.class);

	private static final String INSERT_QUERY = "INSERT INTO `house_scripts` (`house_id`,`script_id`,`script`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE house_id=VALUES(house_id), script_id=VALUES(script_id), script=VALUES(script)";
	private static final String DELETE_QUERY = "DELETE FROM `house_scripts` WHERE `house_id`=? AND `script_id`=?";
	private static final String DELETE_ALL_QUERY = "DELETE FROM `house_scripts` WHERE `house_id`=?";
	private static final String SELECT_QUERY = "SELECT `script_id`, `script` FROM `house_scripts` WHERE `house_id`=? ORDER BY `date_added`";

	public static void storeScript(int houseId, int scriptId, String scriptXML) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
			stmt.setInt(1, houseId);
			stmt.setInt(2, scriptId);
			stmt.setString(3, scriptXML);
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
					addScript(scripts, rset.getInt("script_id"), rset.getString("script"));
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

	private static boolean addScript(PlayerScripts scripts, int id, String scriptXML) {
		if (scriptXML == null || scriptXML.isEmpty()) {
			return scripts.set(id, new byte[0], 0, false);
		} else {
			byte[] bytes = scriptXML.getBytes(StandardCharsets.UTF_16LE);
			byte[] compressedBytes = CompressUtil.compress(bytes);
			return scripts.set(id, compressedBytes, bytes.length, false);
		}
	}

}
