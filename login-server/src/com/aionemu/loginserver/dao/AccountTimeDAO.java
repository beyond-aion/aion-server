package com.aionemu.loginserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.loginserver.model.AccountTime;

/**
 * @author EvilSpirit
 */
public class AccountTimeDAO {

	public static boolean updateAccountTime(int accountId, AccountTime accountTime) {
		return DB.insertUpdate(
			"REPLACE INTO account_time (account_id, last_active, expiration_time, session_duration, accumulated_online, accumulated_rest, penalty_end) values "
				+ "(?,?,?,?,?,?,?)",
			ps -> {
				ps.setLong(1, accountId);
				ps.setTimestamp(2, accountTime.getLastLoginTime());
				ps.setTimestamp(3, accountTime.getExpirationTime());
				ps.setLong(4, accountTime.getSessionDuration());
				ps.setLong(5, accountTime.getAccumulatedOnlineTime());
				ps.setLong(6, accountTime.getAccumulatedRestTime());
				ps.setTimestamp(7, accountTime.getPenaltyEnd());
				ps.execute();
			});
	}

	public static AccountTime getAccountTime(int accountId) {
		AccountTime accountTime = new AccountTime();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement st = con.prepareStatement("SELECT * FROM account_time WHERE account_id = ?")) {
			st.setLong(1, accountId);
			try (ResultSet rs = st.executeQuery()) {
				if (rs.next()) {
					accountTime.setLastLoginTime(rs.getTimestamp("last_active"));
					accountTime.setSessionDuration(rs.getLong("session_duration"));
					accountTime.setAccumulatedOnlineTime(rs.getLong("accumulated_online"));
					accountTime.setAccumulatedRestTime(rs.getLong("accumulated_rest"));
					accountTime.setPenaltyEnd(rs.getTimestamp("penalty_end"));
					accountTime.setExpirationTime(rs.getTimestamp("expiration_time"));
				}
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(AccountTimeDAO.class).error("Can't get account time for account with id: " + accountId, e);
			return null;
		}
		return accountTime;
	}

}
