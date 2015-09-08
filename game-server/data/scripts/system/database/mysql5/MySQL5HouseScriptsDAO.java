package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.HouseScriptsDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.model.gameobjects.player.PlayerScripts;
import com.aionemu.gameserver.utils.xml.CompressUtil;

/**
 * @author Rolandas
 * @modified Neon
 */
public class MySQL5HouseScriptsDAO extends HouseScriptsDAO {

	private static Logger log = LoggerFactory.getLogger(MySQL5HouseScriptsDAO.class);

	public static final String INSERT_QUERY = "INSERT INTO `house_scripts` (`house_id`,`script_id`,`script`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE house_id=VALUES(house_id), script_id=VALUES(script_id), script=VALUES(script)";
	public static final String DELETE_QUERY = "DELETE FROM `house_scripts` WHERE `house_id`=? AND `script_id`=?";
	private static final String SELECT_QUERY = "SELECT `script_id`, `script` FROM `house_scripts` WHERE `house_id`=? ORDER BY `date_added`";

	@Override
	public void storeScript(int houseId, int scriptId, String scriptXML) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
				stmt.setInt(1, houseId);
				stmt.setInt(2, scriptId);
				stmt.setString(3, scriptXML);
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			log.error("Could not save script data for houseId: " + houseId + " in DB: " + e.getMessage(), e);
		}
	}

	@Override
	public PlayerScripts getPlayerScripts(int houseId) {
		PlayerScripts scripts = new PlayerScripts(houseId);
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
				stmt.setInt(1, houseId);
				try (ResultSet rset = stmt.executeQuery()) {
					while (rset.next()) {
						addScript(scripts, rset.getInt("script_id"), rset.getString("script"));
					}
				}
			}
		} catch (Exception e) {
			log.error("Could not restore script data for houseId: " + houseId + " from DB: " + e.getMessage(), e);
		}
		return scripts;
	}

	@Override
	public void deleteScript(int houseId, int scriptId) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DELETE_QUERY)) {
				stmt.setInt(1, houseId);
				stmt.setInt(2, scriptId);
				stmt.executeUpdate();
			}
		} catch (Exception e) {
			log.error("Could not delete script for houseId: " + houseId + " from DB: " + e.getMessage(), e);
		}
	}
	
	private boolean addScript(PlayerScripts scripts, int id, String scriptXML) throws Exception {
		if (scriptXML == null || scriptXML.length() == 0) {
			return scripts.set(id, new byte[0], 0, false);
		} else {
			byte[] bytes = CompressUtil.compress(scriptXML);
			int oldLength = bytes.length;
			bytes = Arrays.copyOf(bytes, bytes.length + 8);
			for (int i = oldLength; i < bytes.length; i++)
				bytes[i] = -51; // Add NC shit bytes, without which fails to load :)
			return scripts.set(id, bytes, scriptXML.length() * 2, false);
		}
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}

}
