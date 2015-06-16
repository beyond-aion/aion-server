package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.PlayerPassportsDAO;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.passport.Passport;
import com.aionemu.gameserver.model.gameobjects.player.passport.PassportsList;

/**
 * @author ViAl
 */
public class MySQL5PlayerPassportsDAO extends PlayerPassportsDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5PlayerPassportsDAO.class);
	public static final String SELECT_QUERY = "SELECT `passportid`, `rewarded`, `arrive_date` FROM `player_passports` WHERE `player_id`=? AND `rewarded`=0";
	public static final String UPDATE_QUERY = "UPDATE `player_passports` SET `rewarded`=? WHERE `player_id`=? AND `passportid`=?";
	public static final String RESET_LASTSTAMPS_QUERY = "UPDATE `players` SET `last_stamp`=NULL";
	public static final String RESET_STAMPS_QUERY = "UPDATE `players` SET `stamps`=0";
	public static final String INSERT_QUERY = "INSERT INTO `player_passports` (`player_id`, `passportid`, `rewarded`, `arrive_date`) VALUES (?,?,?,?)";
	public static final String DELETE_QUERY = "DELETE FROM `player_passports` WHERE player_id = ? AND passportid = ? and arrive_date = ?";
	
	@Override
	public PassportsList load(Player player) {
		PassportsList passportList = new PassportsList();
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY);) {
				stmt.setInt(1, player.getObjectId());
				try (ResultSet rset = stmt.executeQuery();) {
					while (rset.next()) {
						int passportId = rset.getInt("passportid");
						boolean rewarded = rset.getInt("rewarded") != 0;
						Timestamp arriveDate = rset.getTimestamp("arrive_date");
						Passport pp = new Passport(passportId, rewarded, arriveDate);
						pp.setState(PersistentState.UPDATED);
						passportList.addPassport(pp);
					}
				}
			}
		}
		catch (Exception e) {
			log.error("Could not restore completed passport data for player: " + player.getObjectId() + " from DB: " + e.getMessage(), e);
		}
		return passportList;
	}

	@Override
	public void store(Player player) {
		PassportsList pList = player.getCommonData().getPassportsList();
		for(Passport passport : pList.getAllPassports()) {
			switch(passport.getState()) {
				case NEW:
					addPassports(player.getObjectId(), passport);
					break;
				case UPDATE_REQUIRED:
					updatePassport(player.getObjectId(), passport);
					break;
				case DELETED:
					deletePassport(player.getObjectId(), passport);
					break;
			}
			passport.setState(PersistentState.UPDATED);
		}
	}

	private void addPassports(int playerId, Passport passport) {
		try {
			try(Connection conn = DatabaseFactory.getConnection(); PreparedStatement ps = conn.prepareStatement(INSERT_QUERY);) {
				ps.setInt(1, playerId);
				ps.setInt(2, passport.getId());
				ps.setInt(3, passport.isRewarded() ? 1 : 0);
				ps.setTimestamp(4, passport.getArriveDate());
				ps.executeUpdate();
			}
		}
		catch (SQLException e) {
			log.error("Error while adding passports for player "+playerId, e);
		}
	}

	private void updatePassport(int playerId, Passport passport) {
		try {
			try(Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement(UPDATE_QUERY);) {
				ps.setInt(1, passport.isRewarded() ? 1 : 0);
				ps.setInt(2, playerId);
				ps.setInt(3, passport.getId());
				ps.executeUpdate();
			}
		}
		catch (SQLException e) {
			log.error("Failed to update existing passports for player " + playerId, e);
		}
	}
	
	private void deletePassport(int playerId, Passport passport) {
		try {
			try(Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement(DELETE_QUERY);) {
				ps.setInt(1, playerId);
				ps.setInt(2, passport.getId());
				ps.setTimestamp(3, passport.getArriveDate());
				ps.executeUpdate();
			}
		}
		catch (SQLException e) {
			log.error("Failed to delete passports for player " + playerId, e);
		}
	}

	@Override
	public boolean supports(String s, int i, int i1) {
		return MySQL5DAOUtils.supports(s, i, i1);
	}

	@Override
	public void resetAllPassports() {
		try {
			try(Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement(RESET_LASTSTAMPS_QUERY);) {
				ps.executeUpdate();
			}
		}
		catch (SQLException e) {
			log.error("Failed to reset all passports", e);
		}
	}

	@Override
	public void resetAllStamps() {
		try {
			try(Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement(RESET_STAMPS_QUERY);) {
				ps.executeUpdate();
			}
		}
		catch (SQLException e) {
			log.error("Failed to reset all stamps", e);
		}
	}
}
