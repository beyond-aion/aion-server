package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.HouseScriptsDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.model.gameobjects.player.PlayerScripts;

/**
 * @author Rolandas
 */
public class MySQL5HouseScriptsDAO extends HouseScriptsDAO {

	private static Logger log = LoggerFactory.getLogger(MySQL5HouseScriptsDAO.class);

	public static final String INSERT_QUERY = "INSERT INTO `house_scripts` (`house_id`,`index`,`script`) VALUES (?,?,?)";
	public static final String UPDATE_QUERY = "UPDATE `house_scripts` SET `script`=? WHERE `house_id`=? AND `index`=?";
	public static final String DELETE_QUERY = "DELETE FROM `house_scripts` WHERE `house_id`=? AND `index`=?";
	private static final String SELECT_QUERY = "SELECT `index`,`script` FROM `house_scripts` WHERE `house_id`=?";

	@Override
	public void addScript(int houseId, int position, String scriptXML) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
				stmt.setInt(1, houseId);
				stmt.setInt(2, position);
				if (scriptXML == null)
					stmt.setNull(3, Types.LONGNVARCHAR);
				else
					stmt.setString(3, scriptXML);
				stmt.executeUpdate();
			}
		}
		catch (Exception e) {
			log.error("Could not save script data for houseId: " + houseId + " from DB: " + e.getMessage(), e);
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
						int position = rset.getInt("index");
						String scriptXML = rset.getString("script");
						scripts.addScript(position, scriptXML);
					}
				}
			}
		}
		catch (Exception e) {
			log.error("Could not restore script data for houseId: " + houseId + " from DB: " + e.getMessage(), e);
		}
		return scripts;
	}

	@Override
	public void updateScript(int houseId, int position, String scriptXML) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY)) {
				if (scriptXML == null)
					stmt.setNull(1, Types.LONGNVARCHAR);
				else
					stmt.setString(1, scriptXML);
				stmt.setInt(2, houseId);
				stmt.setInt(3, position);
				stmt.executeUpdate();
			}
		}
		catch (Exception e) {
			log.error("Could not save script data for houseId: " + houseId + " from DB: " + e.getMessage(), e);
		}
	}

	@Override
	public void deleteScript(int houseId, int position) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DELETE_QUERY)) {
				stmt.setInt(1, houseId);
				stmt.setInt(2, position);
				stmt.executeUpdate();
			}
		}
		catch (Exception e) {
			log.error("Could not delete script for houseId: " + houseId + " from DB: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}

}
