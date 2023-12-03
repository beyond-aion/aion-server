package com.aionemu.chatserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;

public class ChatLogDAO {

	private static final String INSERT_QUERY = "INSERT INTO `chatlog` (`sender`, `message`, `type`) VALUES (?, ?, ?)";

	public static void save(String sender, String message, String type) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(INSERT_QUERY)) {
			stmt.setString(1, sender);
			stmt.setString(2, message);
			stmt.setString(3, type);
			stmt.execute();
		} catch (Exception e) {
			LoggerFactory.getLogger(ChatLogDAO.class).error("Cannot insert chat message", e);
		}
	}
}
