package com.aionemu.gameserver.dao;

import java.sql.*;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author Neon
 */
public class EventDAO {

	private static final Logger log = LoggerFactory.getLogger(EventDAO.class);

	private static final String SELECT_QUERY = "SELECT `buff_index`, `buff_active_pool_ids`, `buff_allowed_days` FROM `event` WHERE `event_name`=?";
	private static final String INSERT_QUERY = "INSERT INTO `event` (`event_name`, `buff_index`, `buff_active_pool_ids`, `buff_allowed_days`) VALUES (?,?,?,?)";
	private static final String DELETE_QUERY = "DELETE FROM `event` WHERE `event_name`=?";
	private static final String DELETE_OLD_QUERY = "DELETE FROM `event` WHERE last_change < ?";

	public static void deleteOldBuffData() {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement deleteStatement = con.prepareStatement(DELETE_OLD_QUERY)) {
			ZonedDateTime startOfMonth = ServerTime.now().with(LocalTime.MIDNIGHT).withDayOfMonth(1);
			deleteStatement.setTimestamp(1, Timestamp.from(startOfMonth.toInstant()));
			deleteStatement.execute();
		} catch (Exception e) {
			log.error("Couldn't clean up old event buff data", e);
		}
	}

	public static List<StoredBuffData> loadStoredBuffData(String eventName) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setString(1, eventName);
			ResultSet rset = stmt.executeQuery();
			if (!rset.isAfterLast()) {
				List<StoredBuffData> storedBuffData = new ArrayList<>();
				while (rset.next()) {
					int buffIndex = rset.getInt("buff_index");
					Set<Integer> activePoolSkillIds = parseInts(rset.getString("buff_active_pool_ids"));
					Set<Integer> activeRandomDays = parseInts(rset.getString("buff_allowed_days"));
					storedBuffData.add(new StoredBuffData(buffIndex, activePoolSkillIds, activeRandomDays));
				}
				return storedBuffData;
			}
		} catch (SQLException e) {
			log.error("Couldn't load stored event buff info for event: " + eventName, e);
		}
		return null;
	}

	public static boolean storeBuffData(String eventName, List<StoredBuffData> storedBuffData) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement delStmt = con.prepareStatement(DELETE_QUERY);
				 PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
			delStmt.setString(1, eventName);
			delStmt.execute(); // delete all old entries first
			for (StoredBuffData data : storedBuffData) { // store new entries
				stmt.setString(1, eventName);
				stmt.setInt(2, data.getBuffIndex());
				stmt.setString(3, serialize(data.getActivePoolSkillIds()));
				stmt.setString(4, serialize(data.getAllowedBuffDays()));
				stmt.addBatch();
			}
			stmt.executeBatch();
			return true;
		} catch (Exception e) {
			log.error("Couldn't store event buff info for event: " + eventName, e);
			return false;
		}
	}

	private static String serialize(Set<Integer> ints) {
		return ints == null ? null : ints.stream().map(String::valueOf).collect(Collectors.joining(","));
	}

	private static Set<Integer> parseInts(String string) {
		return string == null ? null : Stream.of(string.split(",")).map(Integer::parseInt).collect(Collectors.toSet());
	}

	public static class StoredBuffData {

		private final int buffIndex;
		private final Set<Integer> activePoolSkillIds;
		private final Set<Integer> allowedBuffDays;

		public StoredBuffData(int buffIndex, Set<Integer> activePoolSkillIds, Set<Integer> allowedBuffDays) {
			this.buffIndex = buffIndex;
			this.activePoolSkillIds = activePoolSkillIds;
			this.allowedBuffDays = allowedBuffDays;
		}

		public int getBuffIndex() {
			return buffIndex;
		}

		public Set<Integer> getActivePoolSkillIds() {
			return activePoolSkillIds;
		}

		public Set<Integer> getAllowedBuffDays() {
			return allowedBuffDays;
		}
	}
}
