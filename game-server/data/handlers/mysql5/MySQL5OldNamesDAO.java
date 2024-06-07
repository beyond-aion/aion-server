package mysql5;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DatabaseFactory;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.OldNamesDAO;

/**
 * @author synchro2
 */
public class MySQL5OldNamesDAO extends OldNamesDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5OldNamesDAO.class);

	@Override
	public boolean isNameReserved(String oldName, String newName, int nameReservationDurationDays) {
		if (nameReservationDurationDays > 0) {
			try (Connection con = DatabaseFactory.getConnection();
					 PreparedStatement s = con.prepareStatement("SELECT COUNT(*) cnt FROM old_names WHERE old_name = ? AND COALESCE(new_name != ?, TRUE) AND renamed_date > NOW() - INTERVAL ? DAY")) {
				s.setString(1, newName);
				s.setString(2, oldName);
				s.setInt(3, nameReservationDurationDays);
				ResultSet rs = s.executeQuery();
				rs.next();
				return rs.getInt("cnt") > 0;
			} catch (SQLException e) {
				log.error("Couldn't check if name {} is reserved", newName, e);
			}
		}
		return false;
	}

	@Override
	public void insertNames(int playerId, String oldName, String newName) {
		try (Connection con = DatabaseFactory.getConnection();
				 PreparedStatement stmt = con.prepareStatement("INSERT INTO `old_names` (`player_id`, `old_name`, `new_name`) VALUES (?, ?, ?)")) {
				stmt.setInt(1, playerId);
				stmt.setString(2, oldName);
				stmt.setString(3, newName);
				stmt.execute();
		} catch (SQLException e) {
			log.error("Could not insert names for player {}: {}>{}", playerId, oldName, newName, e);
		}
	}

	@Override
	public boolean supports(String s, int i, int i1) {
		return MySQL5DAOUtils.supports(s, i, i1);
	}
}
