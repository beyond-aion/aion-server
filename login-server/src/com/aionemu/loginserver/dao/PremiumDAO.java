package com.aionemu.loginserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;

/**
 * @author KID
 */
public class PremiumDAO {

	private static final Logger log = LoggerFactory.getLogger("PREMIUM_CTRL");

	public static long getPoints(int accountId) {
		long points = 0;
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement st = con.prepareStatement("SELECT toll FROM account_data WHERE id=?")) {
			st.setInt(1, accountId);
			try (ResultSet rs = st.executeQuery()) {
				if (rs.next()) {
					points = rs.getLong("toll");
				}
			}
		} catch (Exception e) {
			log.error("getPoints [select points] " + accountId, e);
		}

		List<Integer> rewarded = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement st = con.prepareStatement("SELECT uniqId,points FROM account_rewards WHERE accountId=? AND rewarded=0")) {
			st.setInt(1, accountId);
			try (ResultSet rs = st.executeQuery()) {
				if (rs.next()) {
					int uniqId = rs.getInt("uniqId");
					points += rs.getLong("points");
					log.info("Account " + accountId + " has received uniqId #" + uniqId);
					rewarded.add(uniqId);
				}
			}
		} catch (Exception e) {
			log.error("getPoints [get rewards] " + accountId, e);
		}

		if (rewarded.size() > 0) {
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement stmt = con.prepareStatement("UPDATE account_rewards SET rewarded=1,received=NOW() WHERE uniqId=?")) {
				for (int uniqid : rewarded) {
					stmt.setInt(1, uniqid);
					stmt.execute();
				}
			} catch (Exception e) {
				log.error("getPoints [update uniq] " + accountId, e);
			}
		}
		return points;
	}

	public static boolean updatePoints(int accountId, long points, long required) {
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement("UPDATE account_data SET toll=? WHERE id=?")) {
			stmt.setLong(1, points - required);
			stmt.setInt(2, accountId);
			return stmt.executeUpdate() > 0;
		} catch (Exception e) {
			log.error("updatePoints " + accountId, e);
		}
		return false;
	}
}
