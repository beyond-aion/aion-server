package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javolution.util.FastMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.CraftCooldownsDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author synchro2
 */
public class MySQL5CraftCooldownsDAO extends CraftCooldownsDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5CraftCooldownsDAO.class);

	public static final String INSERT_QUERY = "INSERT INTO `craft_cooldowns` (`player_id`, `delay_id`, `reuse_time`) VALUES (?,?,?)";
	public static final String DELETE_QUERY = "DELETE FROM `craft_cooldowns` WHERE `player_id`=?";
	public static final String SELECT_QUERY = "SELECT `delay_id`, `reuse_time` FROM `craft_cooldowns` WHERE `player_id`=?";

	@Override
	public void loadCraftCooldowns(final Player player) {
		FastMap<Integer, Long> craftCoolDowns = new FastMap<Integer, Long>();
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
				stmt.setInt(1, player.getObjectId());
				try (ResultSet rset = stmt.executeQuery()) {
					while (rset.next()) {
						int delayId = rset.getInt("delay_id");
						long reuseTime = rset.getLong("reuse_time");
						int delay = (int) ((reuseTime - System.currentTimeMillis()) / 1000);
						if (delay > 0) {
							craftCoolDowns.put(delayId, reuseTime);
						}
					}
					player.getCraftCooldownList().setCraftCoolDowns(craftCoolDowns);
				}
			}
		}
		catch (SQLException e) {
			log.error("LoadcraftCoolDowns", e);
		}
	}

	@Override
	public void storeCraftCooldowns(final Player player) {
		deleteCraftCoolDowns(player);
		Map<Integer, Long> craftCoolDowns = player.getCraftCooldownList().getCraftCoolDowns();

		if (craftCoolDowns == null)
			return;

		for (Map.Entry<Integer, Long> entry : craftCoolDowns.entrySet()) {
			final int delayId = entry.getKey();
			final long reuseTime = entry.getValue();

			if (reuseTime < System.currentTimeMillis())
				continue;

			Connection con = null;

			try {
				con = DatabaseFactory.getConnection();
				PreparedStatement stmt = con.prepareStatement(INSERT_QUERY);

				stmt.setInt(1, player.getObjectId());
				stmt.setInt(2, delayId);
				stmt.setLong(3, reuseTime);
				stmt.execute();
			}
			catch (SQLException e) {
				log.error("storecraftCoolDowns", e);
			}
			finally {
				DatabaseFactory.close(con);
			}
		}
	}

	private void deleteCraftCoolDowns(final Player player) {
		Connection con = null;

		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(DELETE_QUERY);

			stmt.setInt(1, player.getObjectId());
			stmt.execute();
		}
		catch (SQLException e) {
			log.error("deletecraftCoolDowns", e);
		}
		finally {
			DatabaseFactory.close(con);
		}
	}

	@Override
	public boolean supports(String arg0, int arg1, int arg2) {
		return MySQL5DAOUtils.supports(arg0, arg1, arg2);
	}
}
