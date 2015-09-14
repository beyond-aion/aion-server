package mysql5;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.IUStH;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.OldNamesDAO;

/**
 * @author synchro2
 */

public class MySQL5OldNamesDAO extends OldNamesDAO {

	private static final Logger log = LoggerFactory.getLogger(MySQL5OldNamesDAO.class);

	private static final String INSERT_QUERY = "INSERT INTO `old_names` (`player_id`, `old_name`, `new_name`) VALUES (?,?,?)";

	@Override
	public boolean isOldName(final String name) {
		PreparedStatement s = DB.prepareStatement("SELECT count(player_id) as cnt FROM old_names WHERE ? = old_names.old_name");
		try {
			s.setString(1, name);
			ResultSet rs = s.executeQuery();
			rs.next();
			return rs.getInt("cnt") > 0;
		} catch (SQLException e) {
			log.error("Can't check if name " + name + ", is used, returning possitive result", e);
			return true;
		} finally {
			DB.close(s);
		}
	}

	@Override
	public void insertNames(final int id, final String oldname, final String newname) {
		DB.insertUpdate(INSERT_QUERY, new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, id);
				stmt.setString(2, oldname);
				stmt.setString(3, newname);
				stmt.execute();
			}
		});
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean supports(String s, int i, int i1) {
		return MySQL5DAOUtils.supports(s, i, i1);
	}
}
