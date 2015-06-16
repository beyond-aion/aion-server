package mysql5;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.ServerVariablesDAO;

/**
 * @author Ben
 */
public class MySQL5ServerVariablesDAO extends ServerVariablesDAO {

	private static Logger log = LoggerFactory.getLogger(ServerVariablesDAO.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int load(String var) {
		PreparedStatement ps = DB.prepareStatement("SELECT `value` FROM `server_variables` WHERE `key`=?");
		try {
			ps.setString(1, var);
			ResultSet rs = ps.executeQuery();
			if (rs.next())
				return Integer.parseInt(rs.getString("value"));
		}
		catch (SQLException e) {
			log.error("Error loading last saved server time", e);
		}
		finally {
			DB.close(ps);
		}

		return 0;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean store(String var, int time) {
		boolean success = false;
		PreparedStatement ps = DB.prepareStatement("REPLACE INTO `server_variables` (`key`,`value`) VALUES (?,?)");
		try {
			ps.setString(1, var);
			ps.setString(2, String.valueOf(time));
			success = ps.executeUpdate() > 0;
		}
		catch (SQLException e) {
			log.error("Error storing server time", e);
		}
		finally {
			DB.close(ps);
		}

		return success;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}
}
