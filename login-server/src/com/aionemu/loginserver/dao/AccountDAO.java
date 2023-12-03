package com.aionemu.loginserver.dao;

import java.sql.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.model.AccountTime;

/**
 * @author SoulKeeper, xTz
 */
public class AccountDAO {

	private static final Logger log = LoggerFactory.getLogger(AccountDAO.class);
	private static final String MYSQL_TABLE_ACCOUNT_NAME = Config.useExternalAuth() ? "ext_auth_name" : "name";

	public static Account getAccount(String name) {
		return getAccount("SELECT * FROM account_data WHERE `" + MYSQL_TABLE_ACCOUNT_NAME + "` = ?", name);
	}

	public static Account getAccount(int id) {
		return getAccount("SELECT * FROM account_data WHERE `id` = ?", id);
	}

	private static Account getAccount(String accountQuery, Object accountQueryParam) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement st = con.prepareStatement(accountQuery)) {
			st.setObject(1, accountQueryParam);
			try (ResultSet rs = st.executeQuery()) {
				if (rs.next()) {
					Account account = new Account();
					account.setId(rs.getInt("id"));
					account.setName(rs.getString(MYSQL_TABLE_ACCOUNT_NAME));
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
					return account;
				}
			}
		} catch (SQLException e) {
			log.error("Could not load account for: " + accountQueryParam, e);
		}
		return null;
	}

	public static boolean insertAccount(Account account) {
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement st = con.prepareStatement("INSERT INTO account_data(`" + MYSQL_TABLE_ACCOUNT_NAME
				+ "`, `password`, access_level, membership, activated, last_server, last_ip, last_mac, ip_force, toll) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				Statement.RETURN_GENERATED_KEYS)) {
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
			if (st.executeUpdate() == 0)
				throw new SQLException();
			try (ResultSet rs = st.getGeneratedKeys()) {
				if (!rs.next())
					throw new SQLException("Could not get ID of created account");
				account.setId(rs.getInt(1));
			}
			account.setAccountTime(new AccountTime());
			account.setCreationDate(new Timestamp(System.currentTimeMillis()));
			return true;
		} catch (SQLException e) {
			log.error("Could not insert account for: " + account.getName(), e);
		}
		return false;
	}

	public static boolean updateAccount(Account account) {
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement st = con.prepareStatement("UPDATE account_data SET `" + MYSQL_TABLE_ACCOUNT_NAME
				+ "` = ?, `password` = ?, access_level = ?, membership = ?, last_server = ?, last_ip = ?, last_mac = ?, ip_force = ? WHERE `id` = ?")) {
			st.setString(1, account.getName());
			st.setString(2, account.getPasswordHash());
			st.setByte(3, account.getAccessLevel());
			st.setByte(4, account.getMembership());
			st.setByte(5, account.getLastServer());
			st.setString(6, account.getLastIp());
			st.setString(7, account.getLastMac());
			st.setString(8, account.getIpForce());
			st.setInt(9, account.getId());
			return st.executeUpdate() > 0;
		} catch (SQLException e) {
			log.error("Could not update account for: " + account.getName(), e);
		}
		return false;
	}

	public static boolean updateLastServer(int accountId, byte lastServer) {
		return DB.insertUpdate("UPDATE account_data SET last_server = ? WHERE id = ?", st -> {
			st.setByte(1, lastServer);
			st.setInt(2, accountId);
			st.execute();
		});
	}

	public static boolean updateLastIp(int accountId, String ip) {
		return DB.insertUpdate("UPDATE account_data SET last_ip = ? WHERE id = ?", st -> {
			st.setString(1, ip);
			st.setInt(2, accountId);
			st.execute();
		});
	}

	public static String getLastIp(int accountId) {
		String lastIp = "";
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement st = con.prepareStatement("SELECT `last_ip` FROM `account_data` WHERE `id` = ?")) {
			st.setInt(1, accountId);
			try (ResultSet rs = st.executeQuery()) {
				if (rs.next()) {
					lastIp = rs.getString("last_ip");
				}
			}
		} catch (Exception e) {
			log.error("Can't select last IP of account ID: " + accountId, e);
		}
		return lastIp;
	}

	public static boolean updateLastMac(int accountId, String mac) {
		return DB.insertUpdate("UPDATE `account_data` SET `last_mac` = ? WHERE `id` = ?", st -> {
			st.setString(1, mac);
			st.setInt(2, accountId);
			st.execute();
		});
	}

	public static boolean updateLastHDDSerial(int accountId, String hddSerial) {
		return DB.insertUpdate("UPDATE `account_data` SET `last_hdd_serial` = ? WHERE `id` = ?", st -> {
			st.setString(1, hddSerial);
			st.setInt(2, accountId);
			st.execute();
		});
	}

	public static boolean updateMembership(int accountId) {
		return DB.insertUpdate("UPDATE account_data SET membership = old_membership, expire = NULL WHERE id = ? and expire < CURRENT_TIMESTAMP", st -> {
			st.setInt(1, accountId);
			st.execute();
		});
	}

	public static boolean updateAllowedHDDSerial(int accountId, String hddSerial) {
		return DB.insertUpdate("UPDATE `account_data` SET `allowed_hdd_serial` = ? WHERE `id` = ?", st -> {
			st.setString(1, hddSerial);
			st.setInt(2, accountId);
			st.execute();
		});
	}
}
