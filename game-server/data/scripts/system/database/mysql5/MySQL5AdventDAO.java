package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.AdventDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Neon
 */
public class MySQL5AdventDAO extends AdventDAO {

	@Override
	public int getLastReceivedDay(Player player) {
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement("SELECT `last_day_received` FROM `advent` WHERE ? = account_id")) {
			stmt.setInt(1, player.getAccount().getId());
			ResultSet rset = stmt.executeQuery();
			while (rset.next())
				return rset.getInt("last_day_received");
			return 0;
		} catch (SQLException e) {
			return 0;
		}
	}

	@Override
	public boolean storeLastReceivedDay(Player player, int dayOfMonth) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("REPLACE INTO `advent` VALUES (?, ?)")) {
			stmt.setInt(1, player.getAccount().getId());
			stmt.setInt(2, dayOfMonth);
			stmt.execute();
			return true;
		} catch (SQLException e) {
			return false;
		}
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}
}
