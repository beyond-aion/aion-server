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
import com.aionemu.gameserver.configs.administration.DeveloperConfig;
import com.aionemu.gameserver.configs.main.*;
import com.aionemu.gameserver.configs.network.NetworkConfig;

/**
 * @author Nemesiss, SoulKeeper
 */
public class Config {

	/**
	 * Initialize all configs in com.aionemu.gameserver.configs package
	 */
	public static void load() {
		Properties properties = loadProperties();

		// Administration
		ConfigurableProcessor.process(AdminConfig.class, properties);
		ConfigurableProcessor.process(DeveloperConfig.class, properties);

		// Main
		ConfigurableProcessor.process(AIConfig.class, properties);
		ConfigurableProcessor.process(AutoGroupConfig.class, properties);
		ConfigurableProcessor.process(CommonsConfig.class, properties);
		ConfigurableProcessor.process(CacheConfig.class, properties);
		ConfigurableProcessor.process(CleaningConfig.class, properties);
		ConfigurableProcessor.process(CraftConfig.class, properties);
		ConfigurableProcessor.process(CustomConfig.class, properties);
		ConfigurableProcessor.process(DropConfig.class, properties);
		ConfigurableProcessor.process(EventsConfig.class, properties);
		ConfigurableProcessor.process(FallDamageConfig.class, properties);
		ConfigurableProcessor.process(GSConfig.class, properties);
		ConfigurableProcessor.process(GeoDataConfig.class, properties);
		ConfigurableProcessor.process(GroupConfig.class, properties);
		ConfigurableProcessor.process(HousingConfig.class, properties);
		ConfigurableProcessor.process(HTMLConfig.class, properties);
		ConfigurableProcessor.process(InGameShopConfig.class, properties);
		ConfigurableProcessor.process(LegionConfig.class, properties);
		ConfigurableProcessor.process(LoggingConfig.class, properties);
		ConfigurableProcessor.process(MembershipConfig.class, properties);
		ConfigurableProcessor.process(NameConfig.class, properties);
		ConfigurableProcessor.process(PeriodicSaveConfig.class, properties);
		ConfigurableProcessor.process(PlayerTransferConfig.class, properties);
		ConfigurableProcessor.process(PricesConfig.class, properties);
		ConfigurableProcessor.process(PunishmentConfig.class, properties);
		ConfigurableProcessor.process(RankingConfig.class, properties);
		ConfigurableProcessor.process(RatesConfig.class, properties);
		ConfigurableProcessor.process(SecurityConfig.class, properties);
		ConfigurableProcessor.process(ShutdownConfig.class, properties);
		ConfigurableProcessor.process(SiegeConfig.class, properties);
		ConfigurableProcessor.process(ThreadConfig.class, properties);
		ConfigurableProcessor.process(WorldConfig.class, properties);

		// Network
		ConfigurableProcessor.process(DatabaseConfig.class, properties);
		ConfigurableProcessor.process(NetworkConfig.class, properties);
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
}
