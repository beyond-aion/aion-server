package com.aionemu.loginserver.dao;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.commons.database.DB;
import com.aionemu.loginserver.GameServerInfo;

/**
 * @author -Nemesiss-
 */
public class GameServersDAO {

	public static Map<Byte, GameServerInfo> getAllGameServers() {
		final Map<Byte, GameServerInfo> result = new HashMap<>();
		DB.select("SELECT * FROM gameservers", resultSet -> {
			while (resultSet.next()) {
				byte id = resultSet.getByte("id");
				String ipMask = resultSet.getString("mask");
				String password = resultSet.getString("password");
				GameServerInfo gsi = new GameServerInfo(id, ipMask, password);
				result.put(id, gsi);
			}
		});
		return result;
	}
}
