package com.aionemu.gameserver.configs;

import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.CommonsConfig;
import com.aionemu.commons.configs.DatabaseConfig;
import com.aionemu.commons.configuration.ConfigurableProcessor;
import com.aionemu.commons.utils.PropertiesUtils;
import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.configs.administration.CommandsConfig;
import com.aionemu.gameserver.configs.main.*;
import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.configs.network.PffConfig;

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
		Properties properties = loadProperties();
		for (Class<?> config : CONFIGS) {
			if (allowedConfigs.length == 0 || matches(allowedConfigs, config))
				ConfigurableProcessor.process(config, properties);
		}
	}

	private static boolean matches(Class<?>[] configs, Class<?> config) {
		for (Class<?> c : configs) {
			if (c == config)
				return true;
		}
		return false;
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
