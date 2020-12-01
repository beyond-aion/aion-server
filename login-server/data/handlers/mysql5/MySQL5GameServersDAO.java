package mysql5;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.aionemu.commons.database.DB;
import com.aionemu.commons.database.ReadStH;
import com.aionemu.loginserver.GameServerInfo;
import com.aionemu.loginserver.dao.GameServersDAO;

/**
 * GameServers DAO implementation for MySQL5
 * 
 * @author -Nemesiss-
 */
public class MySQL5GameServersDAO extends GameServersDAO {

	@Override
	public Map<Byte, GameServerInfo> getAllGameServers() {

		final Map<Byte, GameServerInfo> result = new HashMap<>();
		DB.select("SELECT * FROM gameservers", new ReadStH() {

			@Override
			public void handleRead(ResultSet resultSet) throws SQLException {
				while (resultSet.next()) {
					byte id = resultSet.getByte("id");
					String ipMask = resultSet.getString("mask");
					String password = resultSet.getString("password");
					GameServerInfo gsi = new GameServerInfo(id, ipMask, password);
					result.put(id, gsi);
				}
			}
		});
		return result;
	}

	@Override
	public boolean supports(String s, int i, int i1) {
		return MySQL5DAOUtils.supports(s, i, i1);
	}
}
