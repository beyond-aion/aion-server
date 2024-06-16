package com.aionemu.gameserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemCooldown;

/**
 * @author ATracer
 */
public class ItemCooldownsDAO {

	private static final Logger log = LoggerFactory.getLogger(ItemCooldownsDAO.class);

	public static final String INSERT_QUERY = "INSERT INTO `item_cooldowns` (`player_id`, `delay_id`, `use_delay`, `reuse_time`) VALUES (?,?,?,?)";
	public static final String DELETE_QUERY = "DELETE FROM `item_cooldowns` WHERE `player_id`=?";
	public static final String SELECT_QUERY = "SELECT `delay_id`, `use_delay`, `reuse_time` FROM `item_cooldowns` WHERE `player_id`=?";

	public static void loadItemCooldowns(Player player) {
		DB.select(SELECT_QUERY, new ParamReadStH() {

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, player.getObjectId());
			}

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					int delayId = rset.getInt("delay_id");
					int useDelay = rset.getInt("use_delay");
					long reuseTime = rset.getLong("reuse_time");

					if (reuseTime > System.currentTimeMillis())
						player.addItemCoolDown(delayId, reuseTime, useDelay);

				}
			}
		});
		player.getEffectController().broadCastEffects(null);
	}

	public static void storeItemCooldowns(Player player) {
		deleteItemCooldowns(player);

		Map<Integer, ItemCooldown> itemCoolDowns = player.getItemCoolDowns();
		if (itemCoolDowns.isEmpty())
			return;

		itemCoolDowns = new HashMap<>(itemCoolDowns);
		itemCoolDowns.values().removeIf(itemCooldown -> itemCooldown == null || itemCooldown.getReuseTime() - System.currentTimeMillis() <= 30000);

		if (itemCoolDowns.isEmpty())
			return;

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement st = con.prepareStatement(INSERT_QUERY)) {
			con.setAutoCommit(false);

			for (Map.Entry<Integer, ItemCooldown> entry : itemCoolDowns.entrySet()) {
				st.setInt(1, player.getObjectId());
				st.setInt(2, entry.getKey());
				st.setInt(3, entry.getValue().getUseDelay());
				st.setLong(4, entry.getValue().getReuseTime());
				st.addBatch();
			}

			st.executeBatch();
			con.commit();
		} catch (SQLException e) {
			log.error("Error while storing item cooldowns for " + player, e);
		}
	}

	private static void deleteItemCooldowns(Player player) {
		DB.insertUpdate(DELETE_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, player.getObjectId());
				stmt.execute();
			}
		});
	}

}
