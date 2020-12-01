package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.loginserver.dao.AccountsLogDAO;

/**
 * @author ViAl
 * @modified Neon
 */
public class MySQL5AccountsLogDAO extends AccountsLogDAO {

	private static final Logger log = LoggerFactory.getLogger(AccountsLogDAO.class);
	private static final String INSERT_QUERY = "INSERT INTO account_login_history(account_id, gameserver_id, date, ip, mac, hdd_serial) VALUES (?, ?, ?, ?, ?, ?)";

	@Override
	public void addRecord(int accountId, byte gameserverId, long time, String ip, String mac, String hddSerial) {
		try (Connection conn = DatabaseFactory.getConnection(); PreparedStatement stmt = conn.prepareStatement(INSERT_QUERY)) {
			stmt.setInt(1, accountId);
			stmt.setByte(2, gameserverId);
			stmt.setTimestamp(3, new Timestamp(time));
			stmt.setString(4, ip);
			stmt.setString(5, mac);
			stmt.setString(6, hddSerial);
			stmt.execute();
		} catch (Exception e) {
			log.error("Error while inserting account login log.", e);
		}
	}

	@Override
	public boolean supports(String s, int i, int i1) {
		return MySQL5DAOUtils.supports(s, i, i1);
	}
}
