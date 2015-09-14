package com.aionemu.loginserver.configs;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.CommonsConfig;
import com.aionemu.commons.configs.DatabaseConfig;
import com.aionemu.commons.configuration.ConfigurableProcessor;
import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.utils.PropertiesUtils;

/**
 * @author -Nemesiss-, SoulKeeper
 */
public class Config {

	/**
	 * Logger for this class.
	 */
	protected static final Logger log = LoggerFactory.getLogger(Config.class);

	/**
	 * Login Server port
	 */
	@Property(key = "loginserver.network.client.port", defaultValue = "2106")
	public static int LOGIN_PORT;

	/**
	 * Login Server bind IP
	 */
	@Property(key = "loginserver.network.client.host", defaultValue = "localhost")
	public static String LOGIN_BIND_ADDRESS;

	/**
	 * Game Server port
	 */
	@Property(key = "loginserver.network.gameserver.port", defaultValue = "9014")
	public static int GAME_PORT;

	/**
	 * Game Server host
	 */
	@Property(key = "loginserver.network.gameserver.host", defaultValue = "*")
	public static String GAME_BIND_ADDRESS;

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
	 * Set the server on maintenance mode
	 */
	@Property(key = "loginserver.server.maintenance", defaultValue = "false")
	public static boolean MAINTENANCE_MOD;
	/**
	 * Set GM level for maintenance mode
	 */
	@Property(key = "loginserver.server.maintenance.gmlevel", defaultValue = "3")
	public static int MAINTENANCE_MOD_GMLEVEL;

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
	 * Enable\disable checking GS if it is still alive
	 */
	@Property(key = "loginserver.server.pingpong", defaultValue = "true")
	public static boolean ENABLE_PINGPONG;

	/**
	 * Time between checks (in milliseconds)
	 */
	@Property(key = "loginserver.server.pingpong.delay", defaultValue = "3000")
	public static int PINGPONG_DELAY;

	/**
	 * Load configs from files.
	 */
	public static void load() {
		try {
			Properties myProps = null;
			try {
				log.info("Loading: myls.properties");
				myProps = PropertiesUtils.load("./config/myls.properties");
			} catch (Exception e) {
				log.info("No override properties found");
			}

			String network = "./config/network";
			Properties[] props = PropertiesUtils.loadAllFromDirectory(network);
			PropertiesUtils.overrideProperties(props, myProps);

			log.info("Loading: " + network + "/network.properties");
			ConfigurableProcessor.process(Config.class, props);
			log.info("Loading: " + network + "/commons.properties");
			ConfigurableProcessor.process(CommonsConfig.class, props);
			log.info("Loading: " + network + "/database.properties");
			ConfigurableProcessor.process(DatabaseConfig.class, props);

		} catch (Exception e) {
			log.error("Can't load loginserver configuration", e);
			throw new Error("Can't load loginserver configuration", e);
		}
	}
}
