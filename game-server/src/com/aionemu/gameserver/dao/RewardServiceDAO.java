package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.model.templates.rewards.RewardEntryItem;

/**
 * @author KID, Neon
 */
public class RewardServiceDAO {

	private static final Logger log = LoggerFactory.getLogger(RewardServiceDAO.class);

	private static final String UPDATE_QUERY = "UPDATE `player_web_rewards` SET `received`=? WHERE `entry_id`=?";
	private static final String SELECT_QUERY = "SELECT entry_id, item_id, item_count FROM `player_web_rewards` WHERE `player_id`=? AND `received` IS NULL";

	public static List<RewardEntryItem> loadUnreceived(int playerId) {
		List<RewardEntryItem> list = new ArrayList<>();
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, playerId);
			try (ResultSet rset = stmt.executeQuery()) {
				while (rset.next()) {
					int entryId = rset.getInt("entry_id");
					int itemId = rset.getInt("item_id");
					long count = rset.getLong("item_count");
					list.add(new RewardEntryItem(entryId, itemId, count));
				}
			}
		} catch (Exception e) {
			log.error("Couldn't load unreceived web rewards for player " + playerId, e);
		}
		return list;
	}

	public static void storeReceived(List<Integer> ids, long timeReceived) {
		Timestamp time = new Timestamp(timeReceived);
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY)) {
			con.setAutoCommit(false);
			for (int entryId : ids) {
				stmt.setTimestamp(1, time);
				stmt.setInt(2, entryId);
				stmt.addBatch();
			}
			stmt.executeBatch();
			con.commit();
		} catch (Exception e) {
			log.error("Error saving received web rewards, player could potentially receive rewards multiple times! Check entry_id's: "
				+ Arrays.toString(ids.toArray()), e);
		}
	}

}
