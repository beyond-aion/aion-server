package com.aionemu.gameserver.dao;

import java.sql.*;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.account.Account;
import com.aionemu.gameserver.model.account.Passport;
import com.aionemu.gameserver.model.account.PassportsList;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;

/**
 * @author ViAl, Luzien
 */
public class AccountPassportsDAO {

	private static final Logger log = LoggerFactory.getLogger(AccountPassportsDAO.class);

	private static final String SELECT_QUERY = "SELECT `passport_id`, `rewarded`, `arrive_date` FROM `account_passports` WHERE `account_id`=?";
	private static final String UPDATE_QUERY = "UPDATE `account_passports` SET `rewarded`=? WHERE `account_id`=? AND `passport_id`=?";
	private static final String RESET_LASTSTAMPS_QUERY = "UPDATE `account_stamps` SET `last_stamp`=NULL";
	private static final String RESET_STAMPS_QUERY = "UPDATE `account_stamps` SET `stamps`=0";
	private static final String INSERT_QUERY = "INSERT INTO `account_passports` (`account_id`, `passport_id`, `rewarded`, `arrive_date`) VALUES (?,?,?,?)";
	private static final String DELETE_QUERY = "DELETE FROM `account_passports` WHERE account_id = ? AND passport_id = ? and arrive_date = ?";
	private static final String INSERT_STAMPS_QUERY = "INSERT INTO `account_stamps` (`account_id`, `stamps`, `last_stamp`) VALUES (?,?,?)";
	private static final String UPDATE_STAMPS_QUERY = "UPDATE `account_stamps` SET `stamps`= ?, `last_stamp`  = ? WHERE `account_id` = ?";
	private static final String SELECT_STAMPS_QUERY = "SELECT `stamps`, `last_stamp` FROM `account_stamps` WHERE `account_id`=?";

	public static void loadPassport(Account account) {
		PassportsList passportList = new PassportsList();
		try (Connection con = DatabaseFactory.getConnection()) {
			try (PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
				stmt.setInt(1, account.getId());
				ResultSet rset = stmt.executeQuery();
				while (rset.next()) {
					int passport_id = rset.getInt("passport_id");
					boolean rewarded = rset.getInt("rewarded") != 0;
					Timestamp arriveDate = rset.getTimestamp("arrive_date");
					Passport pp = new Passport(passport_id, rewarded, arriveDate);
					pp.setPersistentState(PersistentState.UPDATED);
					passportList.addPassport(pp);
				}
				account.setPassportsList(passportList);
			}
			try (PreparedStatement stmt = con.prepareStatement(SELECT_STAMPS_QUERY)) {
				stmt.setInt(1, account.getId());
				ResultSet rset = stmt.executeQuery();
				int stamps = 0;
				Timestamp lastStamp = null;
				if (rset.next()) {
					stamps = rset.getInt("stamps");
					lastStamp = rset.getTimestamp("last_stamp");
				} else {
					insertStamps(account.getId());
				}
				account.setPassportStamps(stamps);
				account.setLastStamp(lastStamp);
			}
		} catch (Exception e) {
			log.error("Could not restore completed passport data for account: " + account.getId() + " from DB", e);
		}
	}

	public static void storePassportList(int accountId, List<Passport> pList) {
		for (Passport passport : pList) {
			switch (passport.getPersistentState()) {
				case NEW:
					addPassports(accountId, passport);
					break;
				case UPDATE_REQUIRED:
					updatePassport(accountId, passport);
					break;
				case DELETED:
					deletePassport(accountId, passport);
					break;
			}
			passport.setPersistentState(PersistentState.UPDATED);
		}
	}

	public static void storePassport(Account account) {
		storePassportList(account.getId(), account.getPassportsList().getAllPassports());
		updateStamps(account);
	}

	private static void addPassports(int accountId, Passport passport) {
		try (Connection conn = DatabaseFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(INSERT_QUERY)) {
			ps.setInt(1, accountId);
			ps.setInt(2, passport.getId());
			ps.setInt(3, passport.isRewarded() ? 1 : 0);
			ps.setTimestamp(4, passport.getArriveDate());
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Error while adding passports for account " + accountId, e);
		}
	}

	private static void updatePassport(int accountId, Passport passport) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE_QUERY)) {
			ps.setInt(1, passport.isRewarded() ? 1 : 0);
			ps.setInt(2, accountId);
			ps.setInt(3, passport.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Failed to update existing passports for account " + accountId, e);
		}
	}

	private static void deletePassport(int accountId, Passport passport) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE_QUERY)) {
			ps.setInt(1, accountId);
			ps.setInt(2, passport.getId());
			ps.setTimestamp(3, passport.getArriveDate());
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Failed to delete passports for account " + accountId, e);
		}
	}

	private static void insertStamps(int accountId) {
		try (Connection conn = DatabaseFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(INSERT_STAMPS_QUERY)) {
			ps.setInt(1, accountId);
			ps.setInt(2, 0);
			ps.setTimestamp(3, null);
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Error while adding stamos for account " + accountId, e);
		}
	}

	private static void updateStamps(Account account) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE_STAMPS_QUERY)) {
			ps.setInt(1, account.getPassportStamps());
			ps.setTimestamp(2, account.getLastStamp());
			ps.setInt(3, account.getId());
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Failed to update existing passports for account " + account.getId(), e);
		}
	}

	public static void resetAllPassports() {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement(RESET_LASTSTAMPS_QUERY)) {
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Failed to reset all passports", e);
		}
	}

	public static void resetAllStamps() {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement(RESET_STAMPS_QUERY)) {
			ps.executeUpdate();
		} catch (SQLException e) {
			log.error("Failed to reset all stamps", e);
		}
	}

}
