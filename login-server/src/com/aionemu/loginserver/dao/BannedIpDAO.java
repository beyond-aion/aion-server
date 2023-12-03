package com.aionemu.loginserver.dao;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

import com.aionemu.commons.database.DB;
import com.aionemu.loginserver.model.BannedIP;

/**
 * @author SoulKeeper
 */
public class BannedIpDAO {

	public static BannedIP insert(String mask) {
		return insert(mask, null);
	}

	public static BannedIP insert(String mask, Timestamp expireTime) {
		BannedIP result = new BannedIP();
		result.setMask(mask);
		result.setTimeEnd(expireTime);
		return insert(result) ? result : null;
	}

	public static boolean insert(BannedIP bannedIP) {
		return DB.insertUpdate("INSERT INTO banned_ip(mask, time_end) VALUES (?, ?)", ps -> {
			ps.setString(1, bannedIP.getMask());
			ps.setTimestamp(2, bannedIP.getTimeEnd() == null ? null : bannedIP.getTimeEnd());
			ps.execute();
		});
	}

	public static boolean update(BannedIP bannedIP) {
		return DB.insertUpdate("UPDATE banned_ip SET mask = ?, time_end = ? WHERE id = ?", ps -> {
			ps.setString(1, bannedIP.getMask());
			ps.setTimestamp(2, bannedIP.getTimeEnd() == null ? null : bannedIP.getTimeEnd());
			ps.setInt(3, bannedIP.getId());
			ps.execute();
		});
	}

	public static boolean remove(String mask) {
		return DB.insertUpdate("DELETE FROM banned_ip WHERE mask = ?", ps -> {
			ps.setString(1, mask);
			ps.execute();
		});
	}

	public static boolean remove(BannedIP bannedIP) {
		return DB.insertUpdate("DELETE FROM banned_ip WHERE mask = ?", ps -> {
			// Changed from id to mask because we don't get id of last inserted ban
			ps.setString(1, bannedIP.getMask());
			ps.execute();
		});
	}

	public static Set<BannedIP> getAllBans() {
		Set<BannedIP> result = new HashSet<>();
		DB.select("SELECT * FROM banned_ip", rs -> {
			while (rs.next()) {
				BannedIP ip = new BannedIP();
				ip.setId(rs.getInt("id"));
				ip.setMask(rs.getString("mask"));
				ip.setTimeEnd(rs.getTimestamp("time_end"));
				result.add(ip);
			}
		});
		return result;
	}

	public static void cleanExpiredBans() {
		DB.insertUpdate("DELETE FROM banned_ip WHERE time_end < current_timestamp AND time_end IS NOT NULL");
	}
}
