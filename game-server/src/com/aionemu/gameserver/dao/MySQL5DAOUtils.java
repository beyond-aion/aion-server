package com.aionemu.gameserver.dao;

/**
 * @author SoulKeeper
 */
public class MySQL5DAOUtils {

	/**
	 * Constant for MySQL name ;)
	 */
	public static final String MYSQL_DB_NAME = "MySQL";

	/**
	 * Returns true only if DB supports MySQL5
	 * 
	 * @param db
	 *          database name
	 * @param majorVersion
	 *          major version
	 * @param minorVersion
	 *          minor version, ignored
	 * @return supports or not
	 */
	public static boolean supports(String db, int majorVersion, int minorVersion) {
		return MYSQL_DB_NAME.equals(db) && majorVersion == 5;
	}
}
