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
	@Property(key = "gameserver.commands.handler_directories", defaultValue = "./data/handlers/admincommands, ./data/handlers/playercommands, ./data/handlers/consolecommands")
	public static File[] HANDLER_DIRECTORIES;
}
