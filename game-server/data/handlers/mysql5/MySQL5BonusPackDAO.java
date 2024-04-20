package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.BonusPackDAO;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;

/**
 * @author Estrayl, Neon
 */
public class MySQL5BonusPackDAO extends BonusPackDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5BonusPackDAO.class);

	private static final String UPDATE_QUERY = "REPLACE INTO `bonus_packs` (`account_id`, `receiving_player`) VALUES (?,?)";
	private static final String SELECT_QUERY = "SELECT `receiving_player` FROM `bonus_packs` WHERE `account_id`=?";

	@Override
	public int loadReceivingPlayer(int accountId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(SELECT_QUERY)) {
			stmt.setInt(1, accountId);
			ResultSet rset = stmt.executeQuery();
			while (rset.next())
				return rset.getInt("receiving_player");
			return 0;
		} catch (SQLException e) {
			log.error("[BONUS_PACK] Error loading received player id on account id " + accountId, e);
			return Integer.MAX_VALUE;
		}
	}

	@Override
	public boolean storeReceivingPlayer(int accountId, int playerId) {
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement stmt = con.prepareStatement(UPDATE_QUERY)) {
			stmt.setInt(1, accountId);
			stmt.setInt(2, playerId);
			stmt.execute();
			return true;
		} catch (Exception e) {
			log.error("[BONUS_PACK] Error saving received player id " + playerId + " on account id " + accountId, e);
			return false;
		}
	}

	@Override
	public boolean supports(String arg0, int arg1, int arg2) {
		return MySQL5DAOUtils.supports(arg0, arg1, arg2);
	}
}
