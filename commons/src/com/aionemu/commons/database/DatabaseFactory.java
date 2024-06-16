package com.aionemu.commons.database;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;

/**
 * This file is used for creating a pool of connections for the server.<br>
 * It utilizes database.properties and creates a pool of connections and automatically recycles them when closed.<br>
 * 
 * @author Disturbing, SoulKeeper
 */
public class DatabaseFactory {

	/**
	 * Connection Pool holds all connections - Idle or Active
	 */
	private static DataSource dataSource;

	public synchronized static void init() {
		if (dataSource != null) {
			return;
		}

		HikariConfig config = new HikariConfig();
		config.setJdbcUrl(DatabaseConfig.DATABASE_URL);
		config.setUsername(DatabaseConfig.DATABASE_USER);
		config.setPassword(DatabaseConfig.DATABASE_PASSWORD);
		config.setMaximumPoolSize(DatabaseConfig.DATABASE_CONNECTIONS_MAX);
		config.setConnectionTimeout(DatabaseConfig.DATABASE_TIMEOUT);

		dataSource = new HikariDataSource(config);
	}

	/**
	 * @return An active connection from the {@link HikariPool connection pool}
	 * @see HikariDataSource#getConnection()
	 */
	public static Connection getConnection() throws SQLException {
		Connection con = dataSource.getConnection();

		if (!con.getAutoCommit()) {
			LoggerFactory.getLogger(DatabaseFactory.class).error("Connection was not in auto-commit mode.", new IllegalStateException());
			con.setAutoCommit(true);
		}

		return con;
	}

	private DatabaseFactory() {
	}
}
