package com.aionemu.loginserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;

/**
 * @author ViAl
 */
public class AccountsLogDAO {

	public static void addRecord(int accountId, byte gameserverId, long time, String ip, String mac, String hddSerial) {
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con
				.prepareStatement("INSERT INTO account_login_history(account_id, gameserver_id, date, ip, mac, hdd_serial) VALUES (?, ?, ?, ?, ?, ?)")) {
			stmt.setInt(1, accountId);
			stmt.setByte(2, gameserverId);
			stmt.setTimestamp(3, new Timestamp(time));
			stmt.setString(4, ip);
			stmt.setString(5, mac);
			stmt.setString(6, hddSerial);
			stmt.execute();
		} catch (Exception e) {
			LoggerFactory.getLogger(AccountsLogDAO.class).error("Error while inserting account login log.", e);
		}
	}
}
