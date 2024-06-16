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

/**
 * @author nrg
 */
public class PlayerCooldownsDAO {

	private static final Logger log = LoggerFactory.getLogger(PlayerCooldownsDAO.class);

	public static final String INSERT_QUERY = "INSERT INTO `player_cooldowns` (`player_id`, `cooldown_id`, `reuse_delay`) VALUES (?,?,?)";
	public static final String DELETE_QUERY = "DELETE FROM `player_cooldowns` WHERE `player_id`=?";
	public static final String SELECT_QUERY = "SELECT `cooldown_id`, `reuse_delay` FROM `player_cooldowns` WHERE `player_id`=?";

	public static void loadPlayerCooldowns(Player player) {
		DB.select(SELECT_QUERY, new ParamReadStH() {

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, player.getObjectId());
			}

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					int cooldownId = rset.getInt("cooldown_id");
					long reuseDelay = rset.getLong("reuse_delay");

					if (reuseDelay > System.currentTimeMillis())
						player.setSkillCoolDown(cooldownId, reuseDelay);
				}
			}
		});
	}

	public static void storePlayerCooldowns(Player player) {
		deletePlayerCooldowns(player);

		Map<Integer, Long> cooldowns = player.getSkillCoolDowns();
		if (cooldowns == null || cooldowns.isEmpty())
			return;

		cooldowns = new HashMap<>(cooldowns);
		cooldowns.values().removeIf(reuseTime -> reuseTime == null || reuseTime - System.currentTimeMillis() <= 28000);

		if (cooldowns.isEmpty())
			return;

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement st = con.prepareStatement(INSERT_QUERY)) {
			con.setAutoCommit(false);

			for (Map.Entry<Integer, Long> entry : cooldowns.entrySet()) {
				st.setInt(1, player.getObjectId());
				st.setInt(2, entry.getKey());
				st.setLong(3, entry.getValue());
				st.addBatch();
			}

			st.executeBatch();
			con.commit();
		} catch (SQLException e) {
			log.error("Couldn't save cooldowns for " + player, e);
		}
	}

	private static void deletePlayerCooldowns(Player player) {
		DB.insertUpdate(DELETE_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, player.getObjectId());
				stmt.execute();
			}
		});
	}

}
