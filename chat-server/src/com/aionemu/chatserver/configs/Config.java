package com.aionemu.chatserver.configs;

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

	protected static final Logger log = LoggerFactory.getLogger(Config.class);

	/**
	 * Load configs from files.
	 */
	public static void load() {
		try {
			Properties myProps = null;
			try {
				log.info("Loading: ./config/mycs.properties");
				myProps = PropertiesUtils.load("./config/mycs.properties");
			} catch (Exception e) {
				log.info("No override properties found");
			}

			// Main
			String main = "./config/main";
			log.info("Loading: " + main + "/*");
			Properties[] mainProps = PropertiesUtils.loadAllFromDirectory(main);
			PropertiesUtils.overrideProperties(mainProps, myProps);

			ConfigurableProcessor.process(CommonsConfig.class, mainProps);
			ConfigurableProcessor.process(CSConfig.class, mainProps);
			ConfigurableProcessor.process(LoggingConfig.class, mainProps);

			// Network
			String network = "./config/network";
			log.info("Loading: " + network + "/*");
			Properties[] networkProps = PropertiesUtils.loadAllFromDirectory(network);
			PropertiesUtils.overrideProperties(networkProps, myProps);

			ConfigurableProcessor.process(DatabaseConfig.class, networkProps);
			ConfigurableProcessor.process(NetworkConfig.class, networkProps);

		} catch (Exception e) {
			throw new Error("Can't load chatserver configuration:", e);
		}
	}
}
