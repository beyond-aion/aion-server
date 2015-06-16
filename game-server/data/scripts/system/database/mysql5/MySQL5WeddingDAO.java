package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.WeddingDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author synchro2
 */

public class MySQL5WeddingDAO extends WeddingDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5PortalCooldownsDAO.class);

	public static final String INSERT_QUERY = "INSERT INTO `weddings` (`player1`, `player2`) VALUES (?,?)";
	public static final String SELECT_QUERY = "SELECT `player1`, `player2` FROM `weddings` WHERE `player1`=? OR `player2`=?";
	public static final String DELETE_QUERY = "DELETE FROM `weddings` WHERE (`player1`=? AND `player2`=?) OR (`player2`=? AND `player1`=?)";

	@Override
	public int loadPartnerId(final Player player) {
		int playerId = player.getObjectId();
		int partnerId = 0;
		try {
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
				stmt.setInt(1, playerId);
				stmt.setInt(2, playerId);
				try (ResultSet rset = stmt.executeQuery()) {
					int partner1Id = 0;
					int partner2Id = 0;
					if (rset.next()) {
						partner1Id = rset.getInt("player1");
						partner2Id = rset.getInt("player2");
					}
					partnerId = playerId == partner1Id ? partner2Id : partner1Id;
				}
			}
		}
		catch (Exception e) {
			log.error("Could not get partner for player: " + playerId + " from DB: " + e.getMessage(), e);
		}
		return partnerId;
	}

	@Override
	public void storeWedding(final Player partner1, final Player partner2) {
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = DatabaseFactory.getConnection();
			stmt = con.prepareStatement(INSERT_QUERY);

			stmt.setInt(1, partner1.getObjectId());
			stmt.setInt(2, partner2.getObjectId());
			stmt.execute();
		}
		catch (SQLException e) {
			log.error("storeWeddings", e);
		}
		finally {
			DatabaseFactory.close(stmt, con);
		}
	}

	@Override
	public void deleteWedding(final Player partner1, final Player partner2) {
		Connection con = null;
		PreparedStatement stmt = null;
		try {
			con = DatabaseFactory.getConnection();
			stmt = con.prepareStatement(DELETE_QUERY);

			stmt.setInt(1, partner1.getObjectId());
			stmt.setInt(2, partner2.getObjectId());
			stmt.setInt(3, partner1.getObjectId());
			stmt.setInt(4, partner2.getObjectId());
			stmt.execute();
		}
		catch (SQLException e) {
			log.error("deleteWedding", e);
		}
		finally {
			DatabaseFactory.close(stmt, con);
		}
	}

	@Override
	public boolean supports(String arg0, int arg1, int arg2) {
		return MySQL5DAOUtils.supports(arg0, arg1, arg2);
	}
}
