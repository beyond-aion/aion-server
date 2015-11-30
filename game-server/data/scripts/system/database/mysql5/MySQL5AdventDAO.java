package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.AdventDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Nathan
 */
public class MySQL5AdventDAO extends AdventDAO {

	@Override
	public boolean containAllready(Player player) {
		PreparedStatement s = DB.prepareStatement("SELECT count(accName) as cnt FROM advent WHERE ? = advent.accName");
		try {
			s.setString(1, player.getAcountName());
			ResultSet rs = s.executeQuery();
			rs.next();
			return rs.getInt("cnt") != 0;
		} catch (SQLException e) {
			return false;
		} finally {
			DB.close(s);
		}
	}

	@Override
	public int get(Player player) {
		PreparedStatement s = DB.prepareStatement("SELECT date FROM advent WHERE ? = advent.accName AND ? = advent.playerId");
		try {
			s.setString(1, player.getAcountName());
			s.setInt(2, player.getObjectId());
			ResultSet rs = s.executeQuery();
			if (rs.next())
				return rs.getInt("date");
			else
				return -1;
		} catch (SQLException e) {
			return -1;
		} finally {
			DB.close(s);
		}
	}

	@Override
	public void set(Player player, int date) {
		Connection con = null;
		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE advent SET date = ? WHERE accName = ?");

			stmt.setInt(1, date);
			stmt.setString(2, player.getAcountName());
			stmt.execute();
			stmt.close();
		} catch (Exception e) {
		} finally {
			DatabaseFactory.close(con);
		}
	}

	@Override
	public boolean newAdvent(Player player) {
		Connection con = null;
		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement("INSERT INTO advent(accName, playerId, date) VALUES (?, ?, 0)");

			stmt.setString(1, player.getAcountName());
			stmt.setInt(2, player.getObjectId());
			stmt.execute();
			stmt.close();
		} catch (Exception e) {
			return false;
		} finally {
			DatabaseFactory.close(con);
		}
		return true;
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}
}
