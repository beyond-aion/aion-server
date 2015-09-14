package com.aionemu.gameserver.dao;

import java.util.Map;

import com.aionemu.commons.database.dao.DAO;

/**
 * @author KID
 */
public abstract class PlayerVarsDAO implements DAO {

	@Override
	public String getClassName() {
		return PlayerVarsDAO.class.getName();
	}

	public abstract Map<String, Object> load(final int playerId);

	public abstract boolean set(final int playerId, final String key, final Object value);

	public abstract boolean remove(final int playerId, final String key);
}
