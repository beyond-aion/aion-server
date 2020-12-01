package com.aionemu.gameserver.configs.administration;

import java.io.File;
import java.util.Map;

import com.aionemu.commons.configuration.Properties;
import com.aionemu.commons.configuration.Property;

/**
 * @author Neon
 */
public class CommandsConfig {

	@Properties(keyPattern = "^[a-zA-Z0-9_]+$")
	public static Map<String, Byte> ACCESS_LEVELS;

	/**
	 * Location of chat command *.java handlers
	 */
	@Property(key = "gameserver.commands.handler_directories", defaultValue = "./data/scripts/system/handlers/admincommands, ./data/scripts/system/handlers/playercommands, ./data/scripts/system/handlers/consolecommands")
	public static File[] HANDLER_DIRECTORIES;
}
