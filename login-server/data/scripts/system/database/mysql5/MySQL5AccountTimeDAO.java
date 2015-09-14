package mysql5;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.IUStH;
import com.aionemu.loginserver.dao.AccountTimeDAO;
import com.aionemu.loginserver.model.AccountTime;

/**
 * MySQL5 AccountTimeDAO implementation
 * 
 * @author EvilSpirit
 */
public class MySQL5AccountTimeDAO extends AccountTimeDAO {

	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(MySQL5AccountTimeDAO.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean updateAccountTime(final int accountId, final AccountTime accountTime) {
		return DB.insertUpdate("REPLACE INTO account_time (account_id, last_active, expiration_time, "
			+ "session_duration, accumulated_online, accumulated_rest, penalty_end) values " + "(?,?,?,?,?,?,?)",
			new IUStH() {

				@Override
				public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
					preparedStatement.setLong(1, accountId);
					preparedStatement.setTimestamp(2, accountTime.getLastLoginTime());
					preparedStatement.setTimestamp(3, accountTime.getExpirationTime());
					preparedStatement.setLong(4, accountTime.getSessionDuration());
					preparedStatement.setLong(5, accountTime.getAccumulatedOnlineTime());
					preparedStatement.setLong(6, accountTime.getAccumulatedRestTime());
					preparedStatement.setTimestamp(7, accountTime.getPenaltyEnd());
					preparedStatement.execute();
				}
			});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public AccountTime getAccountTime(int accountId) {
		AccountTime accountTime = null;
		PreparedStatement st = DB.prepareStatement("SELECT * FROM account_time WHERE account_id = ?");

		try {
			st.setLong(1, accountId);

			ResultSet rs = st.executeQuery();

			if (rs.next()) {
				accountTime = new AccountTime();

				accountTime.setLastLoginTime(rs.getTimestamp("last_active"));
				accountTime.setSessionDuration(rs.getLong("session_duration"));
				accountTime.setAccumulatedOnlineTime(rs.getLong("accumulated_online"));
				accountTime.setAccumulatedRestTime(rs.getLong("accumulated_rest"));
				accountTime.setPenaltyEnd(rs.getTimestamp("penalty_end"));
				accountTime.setExpirationTime(rs.getTimestamp("expiration_time"));
			}
		}
		catch (Exception e) {
			log.error("Can't get account time for account with id: " + accountId, e);
		}
		finally {
			DB.close(st);
		}

		return accountTime;

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supports(String database, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(database, majorVersion, minorVersion);
	}
}
