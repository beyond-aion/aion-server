package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;

/**
 * @author Ben, Neon
 */
public abstract class ServerVariablesDAO implements DAO {

	@Override
	public final String getClassName() {
		return ServerVariablesDAO.class.getName();
	}

	/**
	 * @return value for given variable as int or null if missing
	 */
	public abstract Integer loadInt(String var);

	/**
	 * @return value for given variable as long or null if missing
	 */
	public abstract Long loadLong(String var);

	/**
	 * Stores the server variable (null is not permitted)
	 */
	public abstract boolean store(String var, Object value);

	/**
	 * Deletes the server variable
	 */
	public abstract boolean delete(String var);

}
