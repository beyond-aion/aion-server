package com.aionemu.commons.database;

import java.sql.CallableStatement;
import java.sql.SQLException;

/**
 * @author ATracer
 */
public interface CallReadStH extends ReadStH {

	/**
	 * @param stmt
	 * @throws SQLException
	 */
	public void setParams(CallableStatement stmt) throws SQLException;
}
