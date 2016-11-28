package com.aionemu.gameserver.dao;

import com.aionemu.commons.database.dao.DAO;

/**
 * @author Ben
 */
public abstract class ServerVariablesDAO implements DAO {

	@Override
	public final String getClassName() {
		return ServerVariablesDAO.class.getName();
	}

	/**
	 * Loads the server variables stored in the database
	 * 
	 * @returns variable stored in database
	 */
	public abstract int load(String var);

	/**
	 * Stores the server variables
	 */
	public abstract boolean store(String var, int value);

}
