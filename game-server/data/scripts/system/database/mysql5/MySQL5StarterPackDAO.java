package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.StarterPackDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Estrayl
 */
public class MySQL5StarterPackDAO extends StarterPackDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5StarterPackDAO.class);

	public static final String INSERT_QUERY = "INSERT INTO `starter_packs` (`account_id`, `receiving_player`) VALUES (?,?)";
	public static final String DELETE_QUERY = "DELETE FROM `starter_packs` WHERE `account_id`=?";
	public static final String SELECT_QUERY = "SELECT `receiving_player` FROM `starter_packs` WHERE `account_id`=?";

	@Override
	public int loadReceivingPlayer(final Player player) {
		final int[] receivingPlayer = new int[1];
		receivingPlayer[0] = 0;

		DB.select(SELECT_QUERY, new ParamReadStH() {

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, player.getPlayerAccount().getId());
			}

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					receivingPlayer[0] = rset.getInt("receiving_player");;
				}
			}
		});

		return receivingPlayer[0];
	}

	@Override
	public void storePlayer(final Player player, final int receivingPlayer) {
		deletePlayer(player);

		Connection con = null;
		try {
			con = DatabaseFactory.getConnection();
			PreparedStatement stmt = con.prepareStatement(INSERT_QUERY);
			stmt.setInt(1, player.getPlayerAccount().getId());
			stmt.setInt(2, receivingPlayer);
			stmt.execute();
			stmt.close();
		} catch (Exception e) {
			log.error("[STARTER_PACK] Error insert levelMask for player " + player.getName(), e);
		} finally {
			DatabaseFactory.close(con);
		}
	}

	private void deletePlayer(final Player player) {
		DB.insertUpdate(DELETE_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, player.getPlayerAccount().getId());
				stmt.execute();
			}
		});
	}

	@Override
	public boolean supports(String arg0, int arg1, int arg2) {
		return MySQL5DAOUtils.supports(arg0, arg1, arg2);
	}
}
