package com.aionemu.commons.database;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Insert/Update Statement handler.<br>
 * For usage details check documentation of DB class.
 * 
 * @author Disturbing
 */
public interface IUStH {

	/**
	 * Enables coder to manually modify statement or batch. Must execute batch or statement manually. Automatically recycles connection.
	 * 
	 * @param stmt
	 * @throws SQLException
	 */
	void handleInsertUpdate(PreparedStatement stmt) throws SQLException;
}
