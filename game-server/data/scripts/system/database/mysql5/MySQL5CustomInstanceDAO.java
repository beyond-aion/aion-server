package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.custom.instance.CustomInstanceRank;
import com.aionemu.gameserver.dao.CustomInstanceDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;

/**
 * @author Jo, Estrayl
 */
public class MySQL5CustomInstanceDAO extends CustomInstanceDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5CustomInstanceDAO.class);
	private static final String SELECT_QUERY = "SELECT * FROM `custom_instance` WHERE ? = player_id";
	private static final String UPDATE_QUERY = "REPLACE INTO `custom_instance` (`player_id`, `rank`, `last_entry`) VALUES (?,?,?)";

	@Override
	public CustomInstanceRank loadPlayerRankObject(int playerId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, playerId);
			ResultSet rset = stmt.executeQuery();
			if (rset.next())
				return new CustomInstanceRank(playerId, rset.getInt("rank"), rset.getTimestamp("last_entry").getTime());
		} catch (SQLException e) {
			log.error("[CUSTOM_INSTANCE] Error loading rank object on player id " + playerId, e);
		}
		return null;
	}

	@Override
	public boolean storePlayer(CustomInstanceRank rankObj) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY)) {
			stmt.setInt(1, rankObj.getPlayerId());
			stmt.setInt(2, rankObj.getRank());
			stmt.setTimestamp(3, new Timestamp(rankObj.getLastEntry()));
			stmt.execute();
			return true;
		} catch (SQLException e) {
			log.error("[CUSTOM_INSTANCE] Error storing last entries on player id " + rankObj.getPlayerId(), e);
			return false;
		}
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}
}
