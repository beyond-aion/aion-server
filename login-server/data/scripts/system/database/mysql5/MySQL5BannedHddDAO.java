package mysql5;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.loginserver.dao.BannedHddDAO;

/**
 * @author ViAl
 */
public class MySQL5BannedHddDAO extends BannedHddDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5BannedHddDAO.class);

	@Override
	public boolean update(String serial, Timestamp time) {
		boolean success = false;
		PreparedStatement ps = DB.prepareStatement("REPLACE INTO `banned_hdd` (`serial`,`time`) VALUES (?,?)");
		try {
			ps.setString(1, serial);
			ps.setTimestamp(2, time);
			success = ps.executeUpdate() > 0;
		} catch (SQLException e) {
			log.error("Error storing hdd serial ban " + serial, e);
		} finally {
			DB.close(ps);
		}
		return success;
	}

	@Override
	public boolean remove(String serial) {
		boolean success = false;
		PreparedStatement ps = DB.prepareStatement("DELETE FROM `banned_hdd` WHERE serial=?");
		try {
			ps.setString(1, serial);
			success = ps.executeUpdate() > 0;
		} catch (SQLException e) {
			log.error("Error removing hdd serial " + serial, e);
		} finally {
			DB.close(ps);
		}
		return success;
	}

	@Override
	public Map<String, Timestamp> load() {
		Map<String, Timestamp> map = new HashMap<>();
		PreparedStatement ps = DB.prepareStatement("SELECT * FROM `banned_hdd`");
		try {
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String serial = rs.getString("serial");
				Timestamp time = rs.getTimestamp("time");
				map.put(serial, time);
			}
		} catch (SQLException e) {
			log.error("Error loading last saved server time", e);
		} finally {
			DB.close(ps);
		}
		return map;
	}

	@Override
	public void cleanExpiredBans() {
		DB.insertUpdate("DELETE FROM `banned_hdd` WHERE time < current_date");
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}

}
