package com.aionemu.gameserver.configs.administration;

import java.util.Map;

import com.aionemu.commons.configuration.Properties;

/**
 * @author Neon
 */
public class CommandsConfig {

	@Properties(keyPattern = "^[a-zA-Z0-9_]+$")
	public static Map<String, Byte> ACCESS_LEVELS;
}
