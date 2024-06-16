package com.aionemu.gameserver.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ReadStH;
import com.aionemu.gameserver.model.Announcement;

/**
 * DAO that manages Announcements
 * 
 * @author Divinity
 */
public class AnnouncementsDAO {

	public static List<Announcement> loadAnnouncements() {
		List<Announcement> result = new ArrayList<>();
		DB.select("SELECT * FROM announcements ORDER BY id", new ReadStH() {

			@Override
			public void handleRead(ResultSet resultSet) throws SQLException {
				while (resultSet.next()) {
					result.add(getAnnouncement(resultSet));
				}
			}
		});
		return result;
	}

	private static Announcement getAnnouncement(ResultSet resultSet) throws SQLException {
		int id = resultSet.getInt("id");
		String message = resultSet.getString("announce").replace("\\n", "\n").replace("\\t", "\t");
		String faction = resultSet.getString("faction");
		String chatType = resultSet.getString("type");
		int delay = resultSet.getInt("delay");
		return new Announcement(id, message, faction, chatType, delay);
	}

	public static int addAnnouncement(String message, String faction, String chatType, int delay) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("INSERT INTO announcements (announce, faction, type, delay) VALUES (?, ?, ?, ?)",
					 Statement.RETURN_GENERATED_KEYS)) {
			stmt.setString(1, message);
			stmt.setString(2, faction);
			stmt.setString(3, chatType);
			stmt.setInt(4, delay);
			stmt.execute();
			try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
				generatedKeys.next();
				return generatedKeys.getInt(1);
			}
		} catch (SQLException e) {
			LoggerFactory.getLogger(AnnouncementsDAO.class).error("", e);
			return -1;
		}
	}

	public static boolean delAnnouncement(int id) {
		return DB.insertUpdate("DELETE FROM announcements WHERE id = ?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement preparedStatement) throws SQLException {
				preparedStatement.setInt(1, id);
				preparedStatement.execute();
			}
		});
	}

}
