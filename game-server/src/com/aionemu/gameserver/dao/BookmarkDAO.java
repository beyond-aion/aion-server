package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;

public class BookmarkDAO {

	public static List<Bookmark> loadBookmarks(int playerId) {
		List<Bookmark> bookmarks = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection();
		     PreparedStatement stmt = con.prepareStatement("SELECT * FROM `bookmark` where player_id= ?")) {
			stmt.setInt(1, playerId);
			stmt.execute();
			ResultSet rs = stmt.getResultSet();
			while (rs.next())
				bookmarks.add(new Bookmark(rs.getString("name"), rs.getInt("world_id"), rs.getFloat("x"), rs.getFloat("y"), rs.getFloat("z")));
		} catch (SQLException e) {
			LoggerFactory.getLogger(BookmarkDAO.class).error("Could not load bookmarks for player: " + playerId, e);
		}
		return bookmarks;
	}

	public static void storeBookmark(int playerId, Bookmark bookmark) {
		try (Connection con = DatabaseFactory.getConnection();
		     PreparedStatement stmt = con.prepareStatement("REPLACE INTO `bookmark` (player_id, name, world_id, x, y, z) VALUES (?, ?, ?, ?, ?, ?)")) {
			stmt.setInt(1, playerId);
			stmt.setString(2, bookmark.name());
			stmt.setInt(3, bookmark.worldId());
			stmt.setFloat(4, bookmark.x());
			stmt.setFloat(5, bookmark.y());
			stmt.setFloat(6, bookmark.z());
			stmt.execute();
		} catch (SQLException e) {
			LoggerFactory.getLogger(BookmarkDAO.class).error("Could not add bookmark for player " + playerId, e);
		}
	}

	public static boolean deleteBookmark(int playerId, String name) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("DELETE FROM `bookmark` WHERE player_id = ? and name = ?")) {
			stmt.setInt(1, playerId);
			stmt.setString(2, name);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			LoggerFactory.getLogger(BookmarkDAO.class).error("Could not delete bookmark " + name + " for player " + playerId, e);
			return false;
		}
	}

	public static void deleteAll(int playerId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement("DELETE FROM `bookmark` WHERE player_id = ?")) {
			stmt.setInt(1, playerId);
			stmt.execute();
		} catch (SQLException e) {
			LoggerFactory.getLogger(BookmarkDAO.class).error("Could not delete all bookmarks", e);
		}
	}

	public record Bookmark(String name, int worldId, float x, float y, float z) {}
}
