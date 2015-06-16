package com.aionemu.commons.database;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Read statement handler.<br>
 * For usage details check documentation of DB class.
 * 
 * @author Disturbing
 */
public interface ReadStH {

	/**
	 * Allows coder to read data after query execution. Automatically recycles connection and closes ResultSet.
	 * 
	 * @param rset
	 * @throws SQLException
	 */
	public void handleRead(ResultSet rset) throws SQLException;
}
