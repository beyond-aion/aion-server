package com.aionemu.loginserver.configs;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.CommonsConfig;
import com.aionemu.commons.configs.DatabaseConfig;
import com.aionemu.commons.configuration.ConfigurableProcessor;
import com.aionemu.commons.configuration.Property;
import com.aionemu.commons.utils.PropertiesUtils;

import ch.qos.logback.classic.ClassicConstants;

/**
 * @author -Nemesiss-, SoulKeeper, Neon
 */
public class Config {

	/**
	 * Local address where LS will listen for Aion client connections (0.0.0.0 = bind any local IP)
	 */
	@Property(key = "loginserver.network.client.socket_address", defaultValue = "0.0.0.0:2106")
	public static InetSocketAddress CLIENT_SOCKET_ADDRESS;

	/**
	 * Local address where LS will listen for GS connections (0.0.0.0 = bind any local IP)
	 */
	@Property(key = "loginserver.network.gameserver.socket_address", defaultValue = "0.0.0.0:9014")
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
	 * URL for external authentication, that is used to receive an JSON encoded string, holding the auth status
	 */
	@Property(key = "loginserver.accounts.external_auth.url", defaultValue = "")
	public static String EXTERNAL_AUTH_URL;

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

	public static boolean useExternalAuth() {
		return !EXTERNAL_AUTH_URL.isBlank();
	}

	/**
	 * Load configs from files.
	 */
	public static void load() {
		Set<String> unusedProperties = ConfigurableProcessor.process(loadProperties(), Config.class, CommonsConfig.class, DatabaseConfig.class);
		if (!unusedProperties.isEmpty()) {
			removePropertiesUsedInLogbackXml(unusedProperties);
			unusedProperties.forEach(unusedProperty -> LoggerFactory.getLogger(Config.class).warn("Config property " + unusedProperty + " is unknown and therefore ignored."));
		}
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

	private static void removePropertiesUsedInLogbackXml(Set<String> properties) {
		String logbackXml = System.getProperty(ClassicConstants.CONFIG_FILE_PROPERTY);
		if (logbackXml != null) {
			try {
				String logbackXmlContent = Files.readString(Path.of(logbackXml));
				properties.removeIf(property -> logbackXmlContent.contains("${" + property + '}'));
			} catch (IOException e) {
				LoggerFactory.getLogger(Config.class).error("", e);
			}
		}
	}
}
