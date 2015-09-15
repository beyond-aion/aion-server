package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.GuideDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.guide.Guide;

/**
 * @author xTz
 */
public class MySQL5GuideDAO extends GuideDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5GuideDAO.class);
	public static final String DELETE_QUERY = "DELETE FROM `guides` WHERE `guide_id`=?";
	public static final String SELECT_QUERY = "SELECT * FROM `guides` WHERE `player_id`=?";
	public static final String SELECT_GUIDE_QUERY = "SELECT * FROM `guides` WHERE `guide_id`=? AND `player_id`=?";

	@Override
	public boolean supports(String arg0, int arg1, int arg2) {
		return MySQL5DAOUtils.supports(arg0, arg1, arg2);
	}

	@Override
	public boolean deleteGuide(int guide_id) {
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(DELETE_QUERY)) {
				stmt.setInt(1, guide_id);
				stmt.execute();
			}
		} catch (Exception e) {
			log.error("Error delete guide_id: " + guide_id, e);
			return false;
		}
		return true;
	}

	@Override
	public List<Guide> loadGuides(int playerId) {
		final List<Guide> guides = new FastTable<Guide>();
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
				stmt.setInt(1, playerId);
				try (ResultSet rset = stmt.executeQuery()) {
					while (rset.next()) {
						int guide_id = rset.getInt("guide_id");
						int player_id = rset.getInt("player_id");
						String title = rset.getString("title");
						Guide guide = new Guide(guide_id, player_id, title);
						guides.add(guide);
					}
				}
			}
		} catch (Exception e) {
			log.error("Could not restore Guide data for player: " + playerId + " from DB: " + e.getMessage(), e);
		}
		return guides;
	}

	@Override
	public Guide loadGuide(int player_id, int guide_id) {
		Guide guide = null;
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_GUIDE_QUERY)) {
				stmt.setInt(1, guide_id);
				stmt.setInt(2, player_id);
				try (ResultSet rset = stmt.executeQuery()) {
					while (rset.next()) {
						String title = rset.getString("title");
						guide = new Guide(guide_id, player_id, title);
					}
				}
			}
		} catch (Exception e) {
			log.error("Could not restore Survey data for player: " + player_id + " from DB: " + e.getMessage(), e);
		}
		return guide;
	}

	@Override
	public void saveGuide(int guide_id, Player player, String title) {
		try {
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement stmt = con.prepareStatement("INSERT INTO guides(guide_id, title, player_id)" + "VALUES (?, ?, ?)")) {
				stmt.setInt(1, guide_id);
				stmt.setString(2, title);
				stmt.setInt(3, player.getObjectId());
				stmt.execute();
			}
		} catch (Exception e) {
			log.error("Error saving playerName: " + player, e);
		}
	}

	@Override
	public int[] getUsedIDs() {
		PreparedStatement statement = DB.prepareStatement("SELECT guide_id FROM guides", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

		try {
			ResultSet rs = statement.executeQuery();
			rs.last();
			int count = rs.getRow();
			rs.beforeFirst();
			int[] ids = new int[count];
			for (int i = 0; i < count; i++) {
				rs.next();
				ids[i] = rs.getInt("guide_id");
			}
			return ids;
		} catch (SQLException e) {
			log.error("Can't get list of id's from guides table", e);
		} finally {
			DB.close(statement);
		}
		return new int[0];
	}
}
