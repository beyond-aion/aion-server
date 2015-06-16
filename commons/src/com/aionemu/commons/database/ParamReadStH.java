package com.aionemu.commons.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Read statement handler.<br>
 * Allows to set query params before execution.<br>
 * For usage details check documentation of DB class.
 * 
 * @author Disturbing
 */
public interface ParamReadStH extends ReadStH {

	/**
	 * Enables coder to manually modify statement parameters.
	 * 
	 * @param stmt
	 * @throws SQLException
	 */
	public void setParams(PreparedStatement stmt) throws SQLException;
}
