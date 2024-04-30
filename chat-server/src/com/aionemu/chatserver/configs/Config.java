package com.aionemu.chatserver.configs;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.main.LoggingConfig;
import com.aionemu.chatserver.configs.network.NetworkConfig;
import com.aionemu.commons.configs.CommonsConfig;
import com.aionemu.commons.configs.DatabaseConfig;
import com.aionemu.commons.configuration.ConfigurableProcessor;
import com.aionemu.commons.utils.NetworkUtils;
import com.aionemu.commons.utils.PropertiesUtils;

import ch.qos.logback.classic.ClassicConstants;

/**
 * @author ATracer
 */
public class Config {

	/**
	 * Load configs from files.
	 */
	public static void load() {
		Set<String> unusedProperties = ConfigurableProcessor.process(loadProperties(), CommonsConfig.class, LoggingConfig.class, DatabaseConfig.class, NetworkConfig.class);
		if (!unusedProperties.isEmpty()) {
			removePropertiesUsedInLogbackXml(unusedProperties);
			unusedProperties.forEach(p -> LoggerFactory.getLogger(Config.class).warn("Config property " + p + " is unknown and therefore ignored."));
		}

		if (NetworkConfig.CLIENT_CONNECT_ADDRESS.getAddress().isAnyLocalAddress()) {
			InetAddress localIPv4 = NetworkUtils.findLocalIPv4();
			if (localIPv4 == null)
				throw new Error("No connect IP for Aion client configured and local IP discovery failed. Please configure chatserver.network.client.connect_address");
			NetworkConfig.CLIENT_CONNECT_ADDRESS = new InetSocketAddress(localIPv4, NetworkConfig.CLIENT_CONNECT_ADDRESS.getPort());
			LoggerFactory.getLogger(Config.class).info("No connect IP for Aion client configured, using " + localIPv4.getHostAddress());
		}
	}

	private static Properties loadProperties() {
		Logger log = LoggerFactory.getLogger(Config.class);
		List<String> defaultsFolders = Arrays.asList("./config/main", "./config/network");
		Properties defaults = new Properties();
		try {
			for (String configDir : defaultsFolders) {
				log.info("Loading default configuration values from: {}/*", configDir);
				PropertiesUtils.loadFromDirectory(defaults, configDir, false);
			}
			log.info("Loading: ./config/mycs.properties");
			Properties properties = PropertiesUtils.load("./config/mycs.properties", defaults);
			if (properties.isEmpty())
				log.info("No override properties found");
			return properties;
		} catch (Exception e) {
			throw new Error("Can't load chatserver configuration:", e);
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
