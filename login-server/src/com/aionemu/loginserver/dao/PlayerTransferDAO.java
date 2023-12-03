package com.aionemu.loginserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.loginserver.service.ptransfer.PlayerTransferTask;

/**
 * @author KID
 */
public class PlayerTransferDAO {

	public static List<PlayerTransferTask> getNew() {
		List<PlayerTransferTask> list = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement st = con.prepareStatement("SELECT * FROM player_transfers WHERE `status` = ?")) {
			st.setInt(1, 0);
			ResultSet rs = st.executeQuery();
			while (rs.next()) {
				PlayerTransferTask task = new PlayerTransferTask();
				task.id = rs.getInt("id");
				task.sourceServerId = (byte) rs.getShort("source_server");
				task.targetServerId = (byte) rs.getShort("target_server");
				task.sourceAccountId = rs.getInt("source_account_id");
				task.targetAccountId = rs.getInt("target_account_id");
				task.playerId = rs.getInt("player_id");
				list.add(task);
			}
		} catch (Exception e) {
			LoggerFactory.getLogger(PlayerTransferDAO.class).error("Can't select getNew: ", e);
		}
		return list;
	}

	public static boolean update(PlayerTransferTask task) {
		String table = "";
		switch (task.status) {
			case PlayerTransferTask.STATUS_ACTIVE:
				table = ", time_performed=NOW()";
				break;
			case PlayerTransferTask.STATUS_DONE:
			case PlayerTransferTask.STATUS_ERROR:
				table = ", time_done=NOW()";
				break;
		}
		return DB.insertUpdate("UPDATE player_transfers SET status=?, comment=?" + table + " WHERE id=?", preparedStatement -> {
			preparedStatement.setByte(1, task.status);
			preparedStatement.setString(2, task.comment);
			preparedStatement.setInt(3, task.id);
			preparedStatement.execute();
		});
	}

}
