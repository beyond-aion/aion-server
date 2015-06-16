package com.aionemu.commons.database.dao;

/**
 * This class represents basic DAO. It should be subclasses by abstract class and that class has to implement method
 * {@link #getClassName()}.<br>
 * This class must return {@link Class#getName()}, {@link #getClassName()} should be final.<br>
 * DAO subclass must have public no-arg constructor, in other case {@link InstantiationException} will be thrown by
 * {@link com.aionemu.commons.database.dao.DAOManager}
 * 
 * @author SoulKeeper
 */
public interface DAO {

	/**
	 * Unique identifier for DAO class, all subclasses must have same identifiers. Must return {@link Class#getName()} of
	 * abstract class
	 * 
	 * @return identifier of DAO class
	 */
	public String getClassName();

	/**
	 * Returns true if DAO implementation supports database or false if not. Database information is provided by
	 * {@link java.sql.DatabaseMetaData}
	 * 
	 * @param databaseName
	 *          name of database
	 * @param majorVersion
	 *          major version of database
	 * @param minorVersion
	 *          minor version of database
	 * @return true if database is supported or false in other case
	 */
	public boolean supports(String databaseName, int majorVersion, int minorVersion);
}
