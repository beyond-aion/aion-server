package com.aionemu.gameserver.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.model.gameobjects.player.BlockList;
import com.aionemu.gameserver.model.gameobjects.player.BlockedPlayer;
import com.aionemu.gameserver.services.player.PlayerService;

/**
 * Responsible for saving and loading data on players' block lists
 * 
 * @author Ben
 */
public class BlockListDAO {

	private static Logger log = LoggerFactory.getLogger(BlockListDAO.class);

	public static final String LOAD_QUERY = "SELECT blocked_player, reason FROM blocks WHERE player=?";
	public static final String ADD_QUERY = "INSERT INTO blocks (player, blocked_player, reason) VALUES (?, ?, ?)";
	public static final String DEL_QUERY = "DELETE FROM blocks WHERE player=? AND blocked_player=?";
	public static final String SET_REASON_QUERY = "UPDATE blocks SET reason=? WHERE player=? AND blocked_player=?";

	public static boolean addBlockedUser(int playerObjId, int objIdToBlock, String reason) {
		return DB.insertUpdate(ADD_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, playerObjId);
				stmt.setInt(2, objIdToBlock);
				stmt.setString(3, reason);
				stmt.execute();
			}
		});
	}

	public static boolean delBlockedUser(int playerObjId, int objIdToDelete) {
		return DB.insertUpdate(DEL_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, playerObjId);
				stmt.setInt(2, objIdToDelete);
				stmt.execute();
			}
		});
	}

	public static BlockList load(int playerObjId) {
		Map<Integer, BlockedPlayer> list = new HashMap<>();

		DB.select(LOAD_QUERY, new ParamReadStH() {

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					int blockedOid = rset.getInt("blocked_player");
					String name = PlayerService.getPlayerName(blockedOid);
					if (name == null) {
						log.error("Attempt to load block list for player " + playerObjId + " tried to load a player which does not exist: " + blockedOid);
					} else {
						list.put(blockedOid, new BlockedPlayer(blockedOid, name, rset.getString("reason")));
					}
				}

			}

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, playerObjId);
			}
		});
		return new BlockList(list);
	}

	public static boolean setReason(int playerObjId, int blockedPlayerObjId, String reason) {
		return DB.insertUpdate(SET_REASON_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, reason);
				stmt.setInt(2, playerObjId);
				stmt.setInt(3, blockedPlayerObjId);
				stmt.execute();

			}
		});
	}

}
