package com.aionemu.chatserver.configs;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.main.CSConfig;
import com.aionemu.chatserver.configs.main.LoggingConfig;
import com.aionemu.chatserver.configs.network.NetworkConfig;
import com.aionemu.commons.configs.CommonsConfig;
import com.aionemu.commons.configs.DatabaseConfig;
import com.aionemu.commons.configuration.ConfigurableProcessor;
import com.aionemu.commons.utils.PropertiesUtils;

/**
 * @author ATracer
 */
public class Config {

	/**
	 * Load configs from files.
	 */
	public static void load() {
		Properties properties = loadProperties();

		// Main
		ConfigurableProcessor.process(CommonsConfig.class, properties);
		ConfigurableProcessor.process(CSConfig.class, properties);
		ConfigurableProcessor.process(LoggingConfig.class, properties);

		// Network
		ConfigurableProcessor.process(DatabaseConfig.class, properties);
		ConfigurableProcessor.process(NetworkConfig.class, properties);
	}

	private static Properties loadProperties() {
		Logger log = LoggerFactory.getLogger(Config.class);
		List<String> defaultsFolders = Arrays.asList("./config/main", "./config/network");
		Properties defaults = new Properties();
		try {
			for (String configDir : defaultsFolders) {
				log.info("Loading default configuration values from: " + configDir + "/*");
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
}
