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
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.PortalCooldownsDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PortalCooldown;

public class MySQL5PortalCooldownsDAO extends PortalCooldownsDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5PortalCooldownsDAO.class);

	public static final String INSERT_QUERY = "INSERT INTO `portal_cooldowns` (`player_id`, `world_id`, `reuse_time`, `entry_count`) VALUES (?,?,?,?)";
	public static final String DELETE_QUERY = "DELETE FROM `portal_cooldowns` WHERE `player_id`=?";
	public static final String SELECT_QUERY = "SELECT `world_id`, `reuse_time`, `entry_count` FROM `portal_cooldowns` WHERE `player_id`=?";

	@Override
	public void loadPortalCooldowns(final Player player) {
		FastMap<Integer, PortalCooldown> portalCoolDowns = new FastMap<Integer, PortalCooldown>();
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
				stmt.setInt(1, player.getObjectId());
				try (ResultSet rset = stmt.executeQuery()) {
					while (rset.next()) {
						int worldId = rset.getInt("world_id");
						long reuseTime = rset.getLong("reuse_time");
						int entryCount = rset.getInt("entry_count");
						if (reuseTime > System.currentTimeMillis()) {
							portalCoolDowns.put(worldId, new PortalCooldown(worldId, reuseTime, entryCount));
						}
					}
					player.getPortalCooldownList().setPortalCoolDowns(portalCoolDowns);
				}
			}
		} catch (SQLException e) {
			log.error("LoadPortalCooldowns", e);
		}
	}

	@Override
	public void storePortalCooldowns(final Player player) {
		deletePortalCooldowns(player);
		Map<Integer, PortalCooldown> portalCoolDowns = player.getPortalCooldownList().getPortalCoolDowns();

		if (portalCoolDowns == null)
			return;

		for (Map.Entry<Integer, PortalCooldown> entry : portalCoolDowns.entrySet()) {
			final int worldId = entry.getKey();
			final long reuseTime = entry.getValue().getReuseTime();
			final int entryCount = entry.getValue().getEnterCount();

			if (reuseTime < System.currentTimeMillis())
				continue;

			Connection con = null;

			PreparedStatement stmt = null;
			try {
				con = DatabaseFactory.getConnection();
				stmt = con.prepareStatement(INSERT_QUERY);

				stmt.setInt(1, player.getObjectId());
				stmt.setInt(2, worldId);
				stmt.setLong(3, reuseTime);
				stmt.setInt(4, entryCount);
				stmt.execute();
			} catch (SQLException e) {
				log.error("storePortalCooldowns", e);
			} finally {
				DatabaseFactory.close(stmt, con);
			}
		}
	}

	private void deletePortalCooldowns(final Player player) {

		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = DatabaseFactory.getConnection();
			stmt = con.prepareStatement(DELETE_QUERY);

			stmt.setInt(1, player.getObjectId());
			stmt.execute();
		} catch (SQLException e) {
			log.error("deletePortalCooldowns", e);
		} finally {
			DatabaseFactory.close(stmt, con);
		}
	}

	@Override
	public boolean supports(String arg0, int arg1, int arg2) {
		return MySQL5DAOUtils.supports(arg0, arg1, arg2);
	}
}
