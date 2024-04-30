package com.aionemu.gameserver.configs;

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

import com.aionemu.commons.configs.CommonsConfig;
import com.aionemu.commons.configs.DatabaseConfig;
import com.aionemu.commons.configuration.ConfigurableProcessor;
import com.aionemu.commons.utils.NetworkUtils;
import com.aionemu.commons.utils.PropertiesUtils;
import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.administration.CommandsConfig;
import com.aionemu.gameserver.configs.main.*;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.configs.network.PffConfig;

import ch.qos.logback.classic.ClassicConstants;

/**
 * @author Nemesiss, SoulKeeper
 */
public class Config {

	private static final List<Class<?>> CONFIGS = Arrays.asList(AdminConfig.class, CommandsConfig.class, AIConfig.class,
		AutoGroupConfig.class, CommonsConfig.class, CleaningConfig.class, CraftConfig.class, CustomConfig.class, DropConfig.class, EventsConfig.class,
		FallDamageConfig.class, GSConfig.class, GeoDataConfig.class, GroupConfig.class, HousingConfig.class, HTMLConfig.class, InGameShopConfig.class,
		InstanceConfig.class, LegionConfig.class, LoggingConfig.class, MembershipConfig.class, NameConfig.class, PeriodicSaveConfig.class,
		PlayerTransferConfig.class, PricesConfig.class, PunishmentConfig.class, RankingConfig.class, RatesConfig.class, SecurityConfig.class,
		ShutdownConfig.class, SiegeConfig.class, ThreadConfig.class, WorldConfig.class, DatabaseConfig.class, NetworkConfig.class, PffConfig.class);

	/**
	 * Load configs of the given classes or all if allowedConfigs is empty.
	 */
	public static void load(Class<?>... allowedConfigs) {
		load(null, allowedConfigs);
	}

	public static void load(Properties overrideProperties, Class<?>... allowedConfigs) {
		Properties properties = loadProperties();
		if (overrideProperties != null) {
			properties.putAll(overrideProperties);
		}
		for (Class<?> config : allowedConfigs) {
			if (!CONFIGS.contains(config))
				throw new IllegalArgumentException(config + " is not an allowed config");
		}
		boolean processAllConfigs = allowedConfigs.length == 0;
		Set<String> unusedProperties = ConfigurableProcessor.process(properties, processAllConfigs ? CONFIGS.toArray() : allowedConfigs);
		if (processAllConfigs && !unusedProperties.isEmpty()) {
			removePropertiesUsedInLogbackXml(unusedProperties);
			unusedProperties.forEach(p -> LoggerFactory.getLogger(Config.class).warn("Config property " + p + " is unknown and therefore ignored."));
		}

		if (NetworkConfig.CLIENT_CONNECT_ADDRESS.getAddress().isAnyLocalAddress()) {
			InetAddress localIPv4 = NetworkUtils.findLocalIPv4();
			if (localIPv4 == null)
				throw new GameServerError("No IP for Aion client advertisement configured and local IP discovery failed. Please configure gameserver.network.client.connect_address");
			NetworkConfig.CLIENT_CONNECT_ADDRESS = new InetSocketAddress(localIPv4, NetworkConfig.CLIENT_CONNECT_ADDRESS.getPort());
			LoggerFactory.getLogger(Config.class).info("No IP for Aion client advertisement configured, using " + localIPv4.getHostAddress());
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

	private static Properties loadProperties() {
		Logger log = LoggerFactory.getLogger(Config.class);
		List<String> defaultsFolders = Arrays.asList("./config/administration", "./config/main", "./config/network");
		Properties defaults = new Properties();
		try {
			for (String configDir : defaultsFolders) {
				log.info("Loading default configuration values from: " + configDir + "/*");
				PropertiesUtils.loadFromDirectory(defaults, configDir, false);
			}
			log.info("Loading: ./config/mygs.properties");
			Properties properties = PropertiesUtils.load("./config/mygs.properties", defaults);
			if (properties.isEmpty())
				log.info("No override properties found");
			return properties;
		} catch (Exception e) {
			throw new GameServerError("Can't load gameserver configuration:", e);
		}
	}

	public static List<Class<?>> getClasses() {
		return CONFIGS;
	}
}
