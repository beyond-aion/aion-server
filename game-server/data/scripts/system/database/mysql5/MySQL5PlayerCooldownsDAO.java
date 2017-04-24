package mysql5;

/**
 * @author nrg
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.PlayerCooldownsDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

public class MySQL5PlayerCooldownsDAO extends PlayerCooldownsDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5PlayerCooldownsDAO.class);

	public static final String INSERT_QUERY = "INSERT INTO `player_cooldowns` (`player_id`, `cooldown_id`, `reuse_delay`) VALUES (?,?,?)";
	public static final String DELETE_QUERY = "DELETE FROM `player_cooldowns` WHERE `player_id`=?";
	public static final String SELECT_QUERY = "SELECT `cooldown_id`, `reuse_delay` FROM `player_cooldowns` WHERE `player_id`=?";

	private static final Predicate<Long> cooldownPredicate = new Predicate<Long>() {

		@Override
		public boolean apply(Long input) {
			return input != null && input - System.currentTimeMillis() > 28000;
		}
	};

	@Override
	public void loadPlayerCooldowns(final Player player) {
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

	@Override
	public void storePlayerCooldowns(final Player player) {
		deletePlayerCooldowns(player);

		Map<Integer, Long> cooldowns = player.getSkillCoolDowns();
		if (cooldowns != null && cooldowns.size() > 0) {
			Map<Integer, Long> filteredCooldown = Maps.filterValues(cooldowns, cooldownPredicate);

			if (filteredCooldown.isEmpty()) {
				return;
			}

			Connection con = null;
			PreparedStatement st = null;
			try {
				con = DatabaseFactory.getConnection();
				con.setAutoCommit(false);
				st = con.prepareStatement(INSERT_QUERY);

				for (Map.Entry<Integer, Long> entry : filteredCooldown.entrySet()) {
					st.setInt(1, player.getObjectId());
					st.setInt(2, entry.getKey());
					st.setLong(3, entry.getValue());
					st.addBatch();
				}

				st.executeBatch();
				con.commit();

			} catch (SQLException e) {
				log.error("Can't save cooldowns for player " + player.getObjectId());
			} finally {
				DatabaseFactory.close(st, con);
			}
		}
	}

	private void deletePlayerCooldowns(final Player player) {
		DB.insertUpdate(DELETE_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, player.getObjectId());
				stmt.execute();
			}
		});
	}

	@Override
	public boolean supports(String arg0, int arg1, int arg2) {
		return MySQL5DAOUtils.supports(arg0, arg1, arg2);
	}
}
