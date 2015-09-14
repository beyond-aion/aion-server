package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.RewardServiceDAO;
import com.aionemu.gameserver.model.templates.rewards.RewardEntryItem;

/**
 * @author KID
 */
public class MySQL5RewardServiceDAO extends RewardServiceDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5RewardServiceDAO.class);
	public static final String UPDATE_QUERY = "UPDATE `web_reward` SET `rewarded`=?, received=NOW() WHERE `unique`=?";
	public static final String SELECT_QUERY = "SELECT * FROM `web_reward` WHERE `item_owner`=? AND `rewarded`=?";

	@Override
	public boolean supports(String arg0, int arg1, int arg2) {
		return MySQL5DAOUtils.supports(arg0, arg1, arg2);
	}

	@Override
	public FastTable<RewardEntryItem> getAvailable(int playerId) {
		FastTable<RewardEntryItem> list = new FastTable<>();
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
				stmt.setInt(1, playerId);
				stmt.setInt(2, 0);
				try (ResultSet rset = stmt.executeQuery()) {
					while (rset.next()) {
						int unique = rset.getInt("unique");
						int item_id = rset.getInt("item_id");
						long count = rset.getLong("item_count");
						list.add(new RewardEntryItem(unique, item_id, count));
					}
				}
			}
		} catch (Exception e) {
			log.warn("getAvailable() for " + playerId + " from DB: " + e.getMessage(), e);
		}
		return list;
	}

	@Override
	public void uncheckAvailable(FastTable<Integer> ids) {
		Connection con = null;
		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt;
			for (int uniqid : ids) {
				stmt = con.prepareStatement(UPDATE_QUERY);
				stmt.setInt(1, 1);
				stmt.setInt(2, uniqid);
				stmt.execute();
				stmt.close();
			}
		} catch (Exception e) {
			log.error("uncheckAvailable", e);
		} finally {
			DatabaseFactory.close(con);
		}
	}
}
