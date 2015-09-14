package com.aionemu.commons.configs;

import java.io.File;

import com.aionemu.commons.configuration.Property;

/**
 * This class holds all configuration of database
 * 
 * @author SoulKeeper
 */
public class DatabaseConfig {

	/**
	 * Default database url.
	 */
	@Property(key = "database.url", defaultValue = "jdbc:mysql://localhost:3306/aion_uni")
	public static String DATABASE_URL;

	/**
	 * Name of database Driver
	 */
	@Property(key = "database.driver", defaultValue = "com.mysql.jdbc.jdbc2.optional.MysqlDataSource")
	public static Class<?> DATABASE_DRIVER;

	/**
	 * Default database user
	 */
	@Property(key = "database.user", defaultValue = "root")
	public static String DATABASE_USER;

	/**
	 * Default database password
	 */
	@Property(key = "database.password", defaultValue = "root")
	public static String DATABASE_PASSWORD;

	@Property(key = "database.connectionpool.timeout", defaultValue = "5000")
	public static int DATABASE_TIMEOUT = 5000;

	/**
	 * Maximum amount of connections that are allowed to use in bonecp partition
	 */
	@Property(key = "database.connectionpool.connections.max", defaultValue = "5")
	public static int DATABASE_CONNECTIONS_MAX;

	/**
	 * Location of database script context descriptor
	 */
	@Property(key = "database.scriptcontext.descriptor", defaultValue = "./data/scripts/system/database/database.xml")
	public static File DATABASE_SCRIPTCONTEXT_DESCRIPTOR;
	
}
