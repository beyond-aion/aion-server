package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.gameserver.model.gameobjects.player.Friend;
import com.aionemu.gameserver.model.gameobjects.player.FriendList;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.services.player.PlayerService;

/**
 * @author Ben
 */
public class FriendListDAO {

	private static final Logger log = LoggerFactory.getLogger(FriendListDAO.class);

	public static final String LOAD_QUERY = "SELECT * FROM `friends` WHERE `player`=?";
	public static final String ADD_QUERY = "INSERT INTO `friends` (`player`,`friend`) VALUES (?, ?)";
	public static final String DEL_QUERY = "DELETE FROM friends WHERE player = ? AND friend = ?";
	public static final String SET_MEMO_QUERY = "UPDATE friends SET memo=? WHERE player=? AND friend=?";

	public static FriendList load(Player player) {
		List<Friend> friends = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(LOAD_QUERY)) {
			stmt.setInt(1, player.getObjectId());
			ResultSet rset = stmt.executeQuery();
			while (rset.next()) {
				int objId = rset.getInt("friend");
				PlayerCommonData pcd = PlayerService.getOrLoadPlayerCommonData(objId);
				if (pcd != null) {
					Friend friend = new Friend(pcd, rset.getString("memo"));
					friends.add(friend);
				}
			}
		} catch (Exception e) {
			log.error("Could not restore FriendList data for player: " + player.getObjectId() + " from DB: " + e.getMessage(), e);
		}

		return new FriendList(player, friends);
	}

	public static boolean addFriends(Player player, Player friend) {
		return DB.insertUpdate(ADD_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement ps) throws SQLException {
				ps.setInt(1, player.getObjectId());
				ps.setInt(2, friend.getObjectId());
				ps.addBatch();

				ps.setInt(1, friend.getObjectId());
				ps.setInt(2, player.getObjectId());
				ps.addBatch();

				ps.executeBatch();
			}
		});

	}

	public static boolean delFriends(int playerOid, int friendOid) {
		return DB.insertUpdate(DEL_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement ps) throws SQLException {
				ps.setInt(1, playerOid);
				ps.setInt(2, friendOid);
				ps.addBatch();

				ps.setInt(1, friendOid);
				ps.setInt(2, playerOid);
				ps.addBatch();

				ps.executeBatch();
			}
		});
	}

	public static boolean setFriendMemo(int playerOid, int friendOid, String memo) {
		return DB.insertUpdate(SET_MEMO_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, memo);
				stmt.setInt(2, playerOid);
				stmt.setInt(3, friendOid);
				stmt.execute();
			}
		});
	}

}
