package com.aionemu.loginserver.configs;

import java.net.InetSocketAddress;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.CommonsConfig;
import com.aionemu.commons.configs.DatabaseConfig;
import com.aionemu.commons.configuration.ConfigurableProcessor;
import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.utils.PropertiesUtils;

/**
 * @author -Nemesiss-, SoulKeeper, Neon
 */
public class Config {

	/**
	 * Local address where LS will listen for Aion client connections (* = bind any local IP)
	 */
	@Property(key = "loginserver.network.client.socket_address", defaultValue = "*:2106")
	public static InetSocketAddress CLIENT_SOCKET_ADDRESS;

	/**
	 * Local address where LS will listen for GS connections (* = bind any local IP)
	 */
	@Property(key = "loginserver.network.gameserver.socket_address", defaultValue = "*:9014")
	public static InetSocketAddress GAMESERVER_SOCKET_ADDRESS;

	/**
	 * Number of login tries before ban
	 */
	@Property(key = "loginserver.network.client.logintrybeforeban", defaultValue = "5")
	public static int LOGIN_TRY_BEFORE_BAN;

	/**
	 * Ban time in minutes
	 */
	@Property(key = "loginserver.network.client.bantimeforbruteforcing", defaultValue = "15")
	public static int WRONG_LOGIN_BAN_TIME;

	/**
	 * Number of threads dedicated to be doing io read & write. There is always 1 acceptor thread. If value is < 1 - acceptor thread will also handle
	 * read & write. If value is > 0 - there will be given amount of read & write threads + 1 acceptor thread.
	 */
	@Property(key = "loginserver.network.nio.threads", defaultValue = "0")
	public static int NIO_READ_WRITE_THREADS;

	/**
	 * Should server automatically create accounts for users or not?
	 */
	@Property(key = "loginserver.accounts.autocreate", defaultValue = "true")
	public static boolean ACCOUNT_AUTO_CREATION;

	/**
	 * Enable\disable external authentication for accounts
	 */
	@Property(key = "loginserver.accounts.externalauth", defaultValue = "false")
	public static boolean AUTH_EXTERNAL;

	/**
	 * URL for external authentication, that is used to receive an JSON encoded string, holding the auth status
	 */
	@Property(key = "loginserver.accounts.externalauth.url", defaultValue = "")
	public static String AUTH_EXTERNAL_JSON_URL;

	/**
	 * Enable\disable flood protector from 1 IP on account login
	 */
	@Property(key = "loginserver.server.floodprotector", defaultValue = "true")
	public static boolean ENABLE_FLOOD_PROTECTION;

	/**
	 * Legal reconnection time. if faster - ban for loginserver.network.client.bantimeforbruteforcing min
	 */
	@Property(key = "loginserver.server.floodprotector.fastreconnection.time", defaultValue = "10")
	public static int FAST_RECONNECTION_TIME;

	/**
	 * IP's excluded from flood protection
	 */
	@Property(key = "loginserver.server.floodprotector.excluded.ips", defaultValue = "")
	public static String EXCLUDED_IP;

	/**
	 * Enable\disable brute-force protector from 1 IP on account login
	 */
	@Property(key = "loginserver.server.bruteforceprotector", defaultValue = "true")
	public static boolean ENABLE_BRUTEFORCE_PROTECTION;

	/**
	 * Log successful gameserver logins including connection data to DB
	 */
	@Property(key = "loginserver.log.logins", defaultValue = "false")
	public static boolean LOG_LOGINS;

	/**
	 * Load configs from files.
	 */
	public static void load() {
		Properties properties = loadProperties();

		ConfigurableProcessor.process(Config.class, properties);
		ConfigurableProcessor.process(CommonsConfig.class, properties);
		ConfigurableProcessor.process(DatabaseConfig.class, properties);
	}

	private static Properties loadProperties() {
		Logger log = LoggerFactory.getLogger(Config.class);
		Properties defaults = new Properties();
		try {
			log.info("Loading default configuration values from: ./config/main/*");
			PropertiesUtils.loadFromDirectory(defaults, "./config/main", false);
			log.info("Loading default configuration values from: ./config/network/*");
			PropertiesUtils.loadFromDirectory(defaults, "./config/network", false);
			log.info("Loading: ./config/myls.properties");
			Properties properties = PropertiesUtils.load("./config/myls.properties", defaults);
			if (properties.isEmpty())
				log.info("No override properties found");
			return properties;
		} catch (Exception e) {
			throw new Error("Can't load loginserver configuration:", e);
		}
	}
}
