package mysql5;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.BlockListDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.PlayerDAO;
import com.aionemu.gameserver.model.gameobjects.player.BlockList;
import com.aionemu.gameserver.model.gameobjects.player.BlockedPlayer;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;

/**
 * @author Ben
 */
public class MySQL5BlockListDAO extends BlockListDAO {

	public static final String LOAD_QUERY = "SELECT blocked_player, reason FROM blocks WHERE player=?";
	public static final String ADD_QUERY = "INSERT INTO blocks (player, blocked_player, reason) VALUES (?, ?, ?)";
	public static final String DEL_QUERY = "DELETE FROM blocks WHERE player=? AND blocked_player=?";
	public static final String SET_REASON_QUERY = "UPDATE blocks SET reason=? WHERE player=? AND blocked_player=?";
	private static Logger log = LoggerFactory.getLogger(MySQL5BlockListDAO.class);

	@Override
	public boolean addBlockedUser(final int playerObjId, final int objIdToBlock, final String reason) {
		return DB.insertUpdate(ADD_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, playerObjId);
				stmt.setInt(2, objIdToBlock);
				stmt.setString(3, reason);
				stmt.execute();
			}
		});
	}

	@Override
	public boolean delBlockedUser(final int playerObjId, final int objIdToDelete) {
		return DB.insertUpdate(DEL_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, playerObjId);
				stmt.setInt(2, objIdToDelete);
				stmt.execute();
			}
		});
	}

	@Override
	public BlockList load(int playerObjId) {
		final Map<Integer, BlockedPlayer> list = new HashMap<Integer, BlockedPlayer>();

		DB.select(LOAD_QUERY, new ParamReadStH() {

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				PlayerDAO playerDao = DAOManager.getDAO(PlayerDAO.class);
				while (rset.next()) {
					int blockedOid = rset.getInt("blocked_player");
					PlayerCommonData pcd = playerDao.loadPlayerCommonData(blockedOid);
					if (pcd == null) {
						log.error("Attempt to load block list for " + playerObjId + " tried to load a player which does not exist: " + blockedOid);
					} else {
						list.put(blockedOid, new BlockedPlayer(pcd, rset.getString("reason")));
					}
				}

			}

			@Override
			public void setParams(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, playerObjId);
			}
		});
		return new BlockList(list);
	}

	@Override
	public boolean setReason(final int playerObjId, final int blockedPlayerObjId, final String reason) {
		return DB.insertUpdate(SET_REASON_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setString(1, reason);
				stmt.setInt(2, playerObjId);
				stmt.setInt(3, blockedPlayerObjId);
				stmt.execute();

			}
		});
	}

	@Override
	public boolean supports(String databaseName, int majorVersion, int minorVersion) {
		return MySQL5DAOUtils.supports(databaseName, majorVersion, minorVersion);
	}
}
