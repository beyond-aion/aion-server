package com.aionemu.gameserver.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.team.legion.*;
import com.aionemu.gameserver.model.team.legion.LegionHistoryAction.Type;

/**
 * Class that is responsible for storing/loading legion data
 * 
 * @author Simple, cura
 */
public class LegionDAO {

	/** Logger */
	private static final Logger log = LoggerFactory.getLogger(LegionDAO.class);
	/** Legion Queries */
	private static final String INSERT_LEGION_QUERY = "INSERT INTO legions(id, `name`) VALUES (?, ?)";
	private static final String SELECT_LEGION_QUERY1 = "SELECT * FROM legions WHERE id=?";
	private static final String SELECT_LEGION_QUERY2 = "SELECT * FROM legions WHERE name=?";
	private static final String DELETE_LEGION_QUERY = "DELETE FROM legions WHERE id = ?";
	private static final String UPDATE_LEGION_QUERY = "UPDATE legions SET name=?, level=?, contribution_points=?, deputy_permission=?, centurion_permission=?, legionary_permission=?, volunteer_permission=?, disband_time=?, occupied_legion_dominion=?, last_legion_dominion=?, current_legion_dominion=? WHERE id=?";
	/** Announcement Queries **/
	private static final String INSERT_ANNOUNCEMENT_QUERY = "INSERT INTO legion_announcement_list(`legion_id`, `announcement`, `date`) VALUES (?, ?, ?)";
	private static final String SELECT_ANNOUNCEMENT_QUERY = "SELECT * FROM legion_announcement_list WHERE legion_id = ? ORDER BY date DESC LIMIT 1";
	private static final String DELETE_ANNOUNCEMENT_QUERY = "DELETE FROM legion_announcement_list WHERE legion_id = ?";
	/** Emblem Queries **/
	private static final String INSERT_EMBLEM_QUERY = "INSERT INTO legion_emblems(legion_id, emblem_id, color_a, color_r, color_g, color_b, emblem_type, emblem_data) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String UPDATE_EMBLEM_QUERY = "UPDATE legion_emblems SET emblem_id=?, color_a=?, color_r=?, color_g=?, color_b=?, emblem_type=?, emblem_data=? WHERE legion_id=?";
	private static final String SELECT_EMBLEM_QUERY = "SELECT * FROM legion_emblems WHERE legion_id=?";
	/** History Queries **/
	private static final String INSERT_HISTORY_QUERY = "INSERT INTO legion_history(`legion_id`, `date`, `history_type`, `name`, `description`) VALUES (?, ?, ?, ?, ?)";
	private static final String SELECT_HISTORY_QUERY = "SELECT * FROM `legion_history` WHERE legion_id=? ORDER BY date DESC, id DESC";
	private static final String DELETE_HISTORY_QUERY = "DELETE FROM `legion_history` WHERE id IN (%s)";

	public static boolean isNameUsed(String name) {
		PreparedStatement s = DB.prepareStatement("SELECT count(id) as cnt FROM legions WHERE ? = legions.name");
		try {
			s.setString(1, name);
			ResultSet rs = s.executeQuery();
			rs.next();
			return rs.getInt("cnt") > 0;
		} catch (SQLException e) {
			log.error("Can't check if name " + name + ", is used, returning possitive result", e);
			return true;
		} finally {
			DB.close(s);
		}
	}

	public static boolean saveNewLegion(Legion legion) {
		boolean success = DB.insertUpdate(INSERT_LEGION_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setInt(1, legion.getLegionId());
				preparedStatement.setString(2, legion.getName());
				preparedStatement.execute();
			}
		});
		return success;
	}

	public static void storeLegion(Legion legion) {
		DB.insertUpdate(UPDATE_LEGION_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, legion.getName());
				stmt.setInt(2, legion.getLegionLevel());
				stmt.setLong(3, legion.getContributionPoints());
				stmt.setInt(4, legion.getDeputyPermission());
				stmt.setInt(5, legion.getCenturionPermission());
				stmt.setInt(6, legion.getLegionaryPermission());
				stmt.setInt(7, legion.getVolunteerPermission());
				stmt.setInt(8, legion.getDisbandTime());
				stmt.setInt(9, legion.getOccupiedLegionDominion());
				stmt.setInt(10, legion.getLastLegionDominion());
				stmt.setInt(11, legion.getCurrentLegionDominion());
				stmt.setInt(12, legion.getLegionId());
				stmt.execute();
			}
		});
	}

	public static Legion loadLegion(String legionName) {
		Legion[] legion = new Legion[1];

		boolean success = DB.select(SELECT_LEGION_QUERY2, new ParamReadStH() {

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, legionName);
			}

			@Override
			public void handleRead(ResultSet resultSet) throws SQLException {
				if (resultSet.next()) {
					legion[0] = new Legion(resultSet.getInt("id"), resultSet.getString("name"));
					legion[0].setLegionLevel(resultSet.getInt("level"));
					legion[0].addContributionPoints(resultSet.getLong("contribution_points"));

					legion[0].setLegionPermissions(resultSet.getShort("deputy_permission"), resultSet.getShort("centurion_permission"),
						resultSet.getShort("legionary_permission"), resultSet.getShort("volunteer_permission"));

					legion[0].setDisbandTime(resultSet.getInt("disband_time"));
					legion[0].setOccupiedLegionDominion(resultSet.getInt("occupied_legion_dominion"));
					legion[0].setLastLegionDominion(resultSet.getInt("last_legion_dominion"));
					legion[0].setCurrentLegionDominion(resultSet.getInt("current_legion_dominion"));
				}
			}
		});

		return success ? legion[0] : null;
	}

	public static Legion loadLegion(int legionId) {
		Legion[] legion = new Legion[1];

		boolean success = DB.select(SELECT_LEGION_QUERY1, new ParamReadStH() {

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, legionId);
			}

			@Override
			public void handleRead(ResultSet resultSet) throws SQLException {
				if (resultSet.next()) {
					legion[0] = new Legion(legionId, resultSet.getString("name"));
					legion[0].setLegionLevel(resultSet.getInt("level"));
					legion[0].addContributionPoints(resultSet.getLong("contribution_points"));

					legion[0].setLegionPermissions(resultSet.getShort("deputy_permission"), resultSet.getShort("centurion_permission"),
						resultSet.getShort("legionary_permission"), resultSet.getShort("volunteer_permission"));

					legion[0].setDisbandTime(resultSet.getInt("disband_time"));
					legion[0].setOccupiedLegionDominion(resultSet.getInt("occupied_legion_dominion"));
					legion[0].setLastLegionDominion(resultSet.getInt("last_legion_dominion"));
					legion[0].setCurrentLegionDominion(resultSet.getInt("current_legion_dominion"));
				}
			}
		});

		return success ? legion[0] : null;
	}

	public static void deleteLegion(int legionId) {
		PreparedStatement statement = DB.prepareStatement(DELETE_LEGION_QUERY);
		try {
			statement.setInt(1, legionId);
		} catch (SQLException e) {
			log.error("deleteLegion #1", e);
		}
		DB.executeUpdateAndClose(statement);
	}

	public static int[] getUsedIDs() {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("SELECT id FROM legions", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
			ResultSet rs = stmt.executeQuery();
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			int[] ids = new int[count];
			for (int i = 0; rs.next(); i++)
				ids[i] = rs.getInt("id");
			return ids;
		} catch (SQLException e) {
			log.error("Can't get list of IDs from legions table", e);
			return null;
		}
	}

	public static Legion.Announcement loadAnnouncement(int legionId) {
		Legion.Announcement announcement = null;
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_ANNOUNCEMENT_QUERY)) {
			stmt.setInt(1, legionId);
			ResultSet resultSet = stmt.executeQuery();
			if (resultSet.next()) {
				String message = resultSet.getString("announcement");
				Timestamp date = resultSet.getTimestamp("date");
				announcement = new Legion.Announcement(message, date);
			}
		} catch (SQLException e) {
			log.error("Couldn't load legion announcements for legion " + legionId, e);
		}
		return announcement;
	}

	public static void saveAnnouncement(int legionId, Legion.Announcement announcement) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement delete = con.prepareStatement(DELETE_ANNOUNCEMENT_QUERY)) {
			delete.setInt(1, legionId);
			delete.executeUpdate();
			if (announcement != null) {
				try (PreparedStatement insert = con.prepareStatement(INSERT_ANNOUNCEMENT_QUERY)) {
					insert.setInt(1, legionId);
					insert.setString(2, announcement.message());
					insert.setTimestamp(3, announcement.time());
					insert.executeUpdate();
				}
			}
		} catch (SQLException e) {
			log.error("Couldn't save announcement for legion " + legionId + ": " + announcement, e);
		}
	}

	public static void storeLegionEmblem(int legionId, LegionEmblem legionEmblem) {
		if (!validEmblem(legionEmblem))
			return;
		if (!(checkEmblem(legionId)))
			createLegionEmblem(legionId, legionEmblem);
		else {
			switch (legionEmblem.getPersistentState()) {
				case UPDATE_REQUIRED:
					updateLegionEmblem(legionId, legionEmblem);
					break;
				case NEW:
					createLegionEmblem(legionId, legionEmblem);
					break;
			}
		}
		legionEmblem.setPersistentState(PersistentState.UPDATED);
	}

	private static boolean validEmblem(LegionEmblem legionEmblem) {
		return legionEmblem.getEmblemType() != LegionEmblemType.CUSTOM || legionEmblem.getCustomEmblemData() != null;
	}

	public static boolean checkEmblem(int legionid) {
		PreparedStatement st = DB.prepareStatement(SELECT_EMBLEM_QUERY);
		try {
			st.setInt(1, legionid);

			ResultSet rs = st.executeQuery();

			if (rs.next())
				return true;
		} catch (SQLException e) {
			log.error("Can't check " + legionid + " legion emblem: ", e);
		} finally {
			DB.close(st);
		}
		return false;
	}

	private static void createLegionEmblem(int legionId, LegionEmblem legionEmblem) {
		DB.insertUpdate(INSERT_EMBLEM_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setInt(1, legionId);
				preparedStatement.setInt(2, legionEmblem.getEmblemId());
				preparedStatement.setByte(3, legionEmblem.getColor_a());
				preparedStatement.setByte(4, legionEmblem.getColor_r());
				preparedStatement.setByte(5, legionEmblem.getColor_g());
				preparedStatement.setByte(6, legionEmblem.getColor_b());
				preparedStatement.setString(7, legionEmblem.getEmblemType().toString());
				preparedStatement.setBytes(8, legionEmblem.getCustomEmblemData());
				preparedStatement.execute();
			}
		});
	}

	private static void updateLegionEmblem(int legionId, LegionEmblem legionEmblem) {
		DB.insertUpdate(UPDATE_EMBLEM_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, legionEmblem.getEmblemId());
				stmt.setByte(2, legionEmblem.getColor_a());
				stmt.setByte(3, legionEmblem.getColor_r());
				stmt.setByte(4, legionEmblem.getColor_g());
				stmt.setByte(5, legionEmblem.getColor_b());
				stmt.setString(6, legionEmblem.getEmblemType().toString());
				stmt.setBytes(7, legionEmblem.getCustomEmblemData());
				stmt.setInt(8, legionId);
				stmt.execute();
			}
		});
	}

	public static LegionEmblem loadLegionEmblem(int legionId) {
		LegionEmblem legionEmblem = new LegionEmblem();

		DB.select(SELECT_EMBLEM_QUERY, new ParamReadStH() {

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, legionId);
			}

			@Override
			public void handleRead(ResultSet resultSet) throws SQLException {
				while (resultSet.next()) {
					legionEmblem.setEmblem(resultSet.getByte("emblem_id"), resultSet.getByte("color_a"), resultSet.getByte("color_r"),
						resultSet.getByte("color_g"), resultSet.getByte("color_b"), LegionEmblemType.valueOf(resultSet.getString("emblem_type")),
						resultSet.getBytes("emblem_data"));
				}
			}
		});
		legionEmblem.setPersistentState(PersistentState.UPDATED);

		return legionEmblem;
	}

	public static void loadHistory(Legion legion) {
		Map<Type, List<LegionHistoryEntry>> history = new EnumMap<>(Type.class);
		for (Type type : Type.values())
			history.put(type, new ArrayList<>());
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_HISTORY_QUERY)) {
			stmt.setInt(1, legion.getLegionId());
			ResultSet resultSet = stmt.executeQuery();
			while (resultSet.next()) {
				int id = resultSet.getInt("id");
				int epochSeconds = (int) (resultSet.getTimestamp("date").getTime() / 1000);
				LegionHistoryAction action = LegionHistoryAction.valueOf(resultSet.getString("history_type"));
				String name = resultSet.getString("name");
				String description = resultSet.getString("description");
				history.get(action.getType()).add(new LegionHistoryEntry(id, epochSeconds, action, name, description));
			}
		} catch (Exception e) {
			log.error("Could not load history of legion " + legion, e);
		}
		legion.setHistory(history);
	}

	public static LegionHistoryEntry insertHistory(int legionId, LegionHistoryAction action, String name, String description) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_HISTORY_QUERY, Statement.RETURN_GENERATED_KEYS)) {
			long nowMillis = System.currentTimeMillis();
			stmt.setInt(1, legionId);
			stmt.setTimestamp(2, new Timestamp(nowMillis));
			stmt.setString(3, action.toString());
			stmt.setString(4, name);
			stmt.setString(5, description);
			stmt.execute();
			ResultSet result = stmt.getGeneratedKeys();
			result.next();
			return new LegionHistoryEntry(result.getInt(1), (int) (nowMillis / 1000), action, name, description);
		} catch (Exception e) {
			log.error("Could not add history entry for legion " + legionId, e);
			return null;
		}
	}

	public static void deleteHistory(int legionId, List<LegionHistoryEntry> entries) {
		if (entries.isEmpty())
			return;
		String placeholders = ",?".repeat(entries.size()).substring(1);
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DELETE_HISTORY_QUERY.formatted(placeholders))) {
			for (int i = 0; i < entries.size(); i++)
				stmt.setInt(i + 1, entries.get(i).id());
			stmt.executeUpdate();
		} catch (Exception e) {
			log.error("Could not delete " + entries.size() + " history entries for legion " + legionId, e);
		}
	}
}
