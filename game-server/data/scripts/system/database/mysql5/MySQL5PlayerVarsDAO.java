package mysql5;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javolution.util.FastMap;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.IUStH;
import com.aionemu.commons.database.ParamReadStH;
import com.aionemu.gameserver.dao.MySQL5DAOUtils;
import com.aionemu.gameserver.dao.PlayerVarsDAO;

/**
 * @author KID
 */
public class MySQL5PlayerVarsDAO extends PlayerVarsDAO {

	@Override
	public Map<String, Object> load(final int playerId) {
		final Map<String, Object> map = FastMap.newInstance();
		DB.select("SELECT param,value FROM player_vars WHERE player_id=?", new ParamReadStH() {

			@Override
			public void handleRead(ResultSet rset) throws SQLException {
				while (rset.next()) {
					String key = rset.getString("param");
					String value = rset.getString("value");
					map.put(key, value);
				}
			}

			@Override
			public void setParams(PreparedStatement st) throws SQLException {
				st.setInt(1, playerId);
			}
		});

		return map;
	}

	@Override
	public boolean set(final int playerId, final String key, final Object value) {
		boolean result = DB.insertUpdate(
			"INSERT INTO player_vars (`player_id`, `param`, `value`, `time`) VALUES (?,?,?,NOW())", new IUStH() {

				@Override
				public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
					stmt.setInt(1, playerId);
					stmt.setString(2, key);
					stmt.setString(3, value.toString());
					stmt.execute();
				}
			});

		return result;
	}

	@Override
	public boolean remove(final int playerId, final String key) {
		boolean result = DB.insertUpdate("DELETE FROM player_vars WHERE player_id=? AND param=?", new IUStH() {

			@Override
			public void handleInsertUpdate(PreparedStatement stmt) throws SQLException {
				stmt.setInt(1, playerId);
				stmt.setString(2, key);
				stmt.execute();
			}
		});

		return result;
	}

	@Override
	public boolean supports(String s, int i, int i1) {
		return MySQL5DAOUtils.supports(s, i, i1);
	}
}
