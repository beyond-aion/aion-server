package mysql5;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.IUStH;
import com.aionemu.loginserver.dao.AccountDAO;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.model.AccountTime;

/**
 * MySQL5 Account DAO implementation
 * 
 * @author SoulKeeper, xTz
 */
public class MySQL5AccountDAO extends AccountDAO {

	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(MySQL5AccountDAO.class);

	@Override
	public Account getAccount(String name) {
		Account account = null;
		PreparedStatement st = DB.prepareStatement("SELECT * FROM account_data WHERE `" + MySQL5DAOUtils.MYSQL_TABLE_ACCOUNT_NAME + "` = ?");

		try {
			st.setString(1, name);

			ResultSet rs = st.executeQuery();

			if (rs.next()) {
				account = new Account();

				account.setId(rs.getInt("id"));
				account.setName(name);
				account.setPasswordHash(rs.getString("password"));
				account.setCreationDate(rs.getTimestamp("creation_date"));
				account.setAccessLevel(rs.getByte("access_level"));
				account.setMembership(rs.getByte("membership"));
				account.setActivated(rs.getByte("activated"));
				account.setLastServer(rs.getByte("last_server"));
				account.setLastIp(rs.getString("last_ip"));
				account.setLastMac(rs.getString("last_mac"));
				account.setIpForce(rs.getString("ip_force"));
				account.setAllowedHddSerial(rs.getString("allowed_hdd_serial"));
			}
		} catch (SQLException e) {
			log.error("Can't select account with name: " + name, e);
		} finally {
			DB.close(st);
		}

		return account;
	}

	@Override
	public Account getAccount(int id) {
		Account account = null;
		PreparedStatement st = DB.prepareStatement("SELECT * FROM account_data WHERE `id` = ?");

		try {
			st.setInt(1, id);

			ResultSet rs = st.executeQuery();

			if (rs.next()) {
				account = new Account();

				account.setId(rs.getInt("id"));
				account.setName(rs.getString(MySQL5DAOUtils.MYSQL_TABLE_ACCOUNT_NAME));
				account.setPasswordHash(rs.getString("password"));
				account.setCreationDate(rs.getTimestamp("creation_date"));
				account.setAccessLevel(rs.getByte("access_level"));
				account.setMembership(rs.getByte("membership"));
				account.setActivated(rs.getByte("activated"));
				account.setLastServer(rs.getByte("last_server"));
				account.setLastIp(rs.getString("last_ip"));
				account.setLastMac(rs.getString("last_mac"));
				account.setIpForce(rs.getString("ip_force"));
				account.setAllowedHddSerial(rs.getString("allowed_hdd_serial"));
			}
		} catch (SQLException e) {
			log.error("Can't select account with name: " + id, e);
		} finally {
			DB.close(st);
		}

		return account;
	}

	@Override
	public int getAccountId(String name) {
		int id = -1;
		PreparedStatement st = DB.prepareStatement("SELECT `id` FROM account_data WHERE `" + MySQL5DAOUtils.MYSQL_TABLE_ACCOUNT_NAME + "` = ?");

		try {
			st.setString(1, name);

			ResultSet rs = st.executeQuery();

			rs.next();

			id = rs.getInt("id");
		} catch (SQLException e) {
			log.error("Can't select id after account insertion", e);
		} finally {
			DB.close(st);
		}

		return id;
	}

	@Override
	public int getAccountCount() {
		PreparedStatement st = DB.prepareStatement("SELECT count(*) AS c FROM account_data");
		ResultSet rs = DB.executeQuerry(st);

		try {
			rs.next();

			return rs.getInt("c");
		} catch (SQLException e) {
			log.error("Can't get account count", e);
		} finally {
			DB.close(st);
		}

		return -1;
	}

	@Override
	public boolean insertAccount(Account account) {
		int result = 0;
		PreparedStatement st = DB.prepareStatement("INSERT INTO account_data(`" + MySQL5DAOUtils.MYSQL_TABLE_ACCOUNT_NAME
			+ "`, `password`, access_level, membership, activated, last_server, last_ip, last_mac, ip_force, toll) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

		try {
			st.setString(1, account.getName());
			st.setString(2, account.getPasswordHash());
			st.setByte(3, account.getAccessLevel());
			st.setByte(4, account.getMembership());
			st.setByte(5, account.getActivated());
			st.setByte(6, account.getLastServer());
			st.setString(7, account.getLastIp());
			st.setString(8, account.getLastMac());
			st.setString(9, account.getIpForce());
			st.setLong(10, 0);
			result = st.executeUpdate();
		} catch (SQLException e) {
			log.error("Can't insert account", e);
		} finally {
			DB.close(st);
		}

		if (result > 0) {
			account.setId(getAccountId(account.getName()));
			account.setAccountTime(new AccountTime());
			account.setCreationDate(new Timestamp(System.currentTimeMillis()));
		}

		return result > 0;
	}

	@Override
	public boolean updateAccount(Account account) {
		PreparedStatement st = DB.prepareStatement("UPDATE account_data SET `" + MySQL5DAOUtils.MYSQL_TABLE_ACCOUNT_NAME
			+ "` = ?, `password` = ?, access_level = ?, membership = ?, last_server = ?, last_ip = ?, last_mac = ?, ip_force = ? WHERE `id` = ?");
		try {
			st.setString(1, account.getName());
			st.setString(2, account.getPasswordHash());
			st.setByte(3, account.getAccessLevel());
			st.setByte(4, account.getMembership());
			st.setByte(5, account.getLastServer());
			st.setString(6, account.getLastIp());
			st.setString(7, account.getLastMac());
			st.setString(8, account.getIpForce());
			st.setInt(9, account.getId());
			st.executeUpdate();
		} catch (SQLException e) {
			log.error("Can't update " + account, e);
			return false;
		} finally {
			DB.close(st);
		}
		return true;
	}

	@Override
	public boolean updateLastServer(final int accountId, final byte lastServer) {
		return DB.insertUpdate("UPDATE account_data SET last_server = ? WHERE id = ?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setByte(1, lastServer);
				preparedStatement.setInt(2, accountId);
				preparedStatement.execute();
			}
		});
	}

	@Override
	public boolean updateLastIp(final int accountId, final String ip) {
		return DB.insertUpdate("UPDATE account_data SET last_ip = ? WHERE id = ?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setString(1, ip);
				preparedStatement.setInt(2, accountId);
				preparedStatement.execute();
			}
		});
	}

	@Override
	public String getLastIp(final int accountId) {
		String lastIp = "";
		PreparedStatement st = DB.prepareStatement("SELECT `last_ip` FROM `account_data` WHERE `id` = ?");

		try {
			st.setInt(1, accountId);
			ResultSet rs = st.executeQuery();
			if (rs.next()) {
				lastIp = rs.getString("last_ip");
			}
		} catch (Exception e) {
			log.error("Can't select last IP of account ID: " + accountId, e);
			return "";
		} finally {
			DB.close(st);
		}

		return lastIp;
	}

	@Override
	public boolean updateLastMac(final int accountId, final String mac) {
		return DB.insertUpdate("UPDATE `account_data` SET `last_mac` = ? WHERE `id` = ?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setString(1, mac);
				preparedStatement.setInt(2, accountId);
				preparedStatement.execute();
			}
		});
	}

	@Override
	public boolean updateLastHDDSerial(final int accountId, final String hddSerial) {
		return DB.insertUpdate("UPDATE `account_data` SET `last_hdd_serial` = ? WHERE `id` = ?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setString(1, hddSerial);
				preparedStatement.setInt(2, accountId);
				preparedStatement.execute();
			}
		});
	}

	@Override
	public boolean updateMembership(final int accountId) {
		return DB.insertUpdate("UPDATE account_data SET membership = old_membership, expire = NULL WHERE id = ? and expire < CURRENT_TIMESTAMP",
			new IUStH() {

				@Override
				public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
					preparedStatement.setInt(1, accountId);
					preparedStatement.execute();
				}
			});
	}

	@Override
	public void deleteInactiveAccounts(int daysOfInactivity) {
		PreparedStatement statement = DB
			.prepareStatement("DELETE FROM account_data WHERE id IN (SELECT account_id FROM account_time WHERE UNIX_TIMESTAMP(CURDATE())-UNIX_TIMESTAMP(last_active) > ? * 24 * 60 * 60)");
		try {
			statement.setInt(1, daysOfInactivity);
		} catch (SQLException e) {
			log.error("Some crap, can't set int parameter to PreparedStatement", e);
		}
		DB.executeUpdateAndClose(statement);
	}

	@Override
	public boolean updateAllowedHDDSerial(int accountId, String hddSerial) {
		return DB.insertUpdate("UPDATE `account_data` SET `allowed_hdd_serial` = ? WHERE `id` = ?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setString(1, hddSerial);
				preparedStatement.setInt(2, accountId);
				preparedStatement.execute();
			}
		});
	}

	@Override
	public boolean supports(String database, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(database, majorVersion, minorVersion);
	}
}
