package com.aionemu.gameserver.configs.main;

import java.io.File;
import java.util.Set;

import com.aionemu.commons.configuration.Property;

public class InstanceConfig {

	@Property(key = "gameserver.instance.cooldown_rate", defaultValue = "1")
	public static int INSTANCE_COOLDOWN_RATE;

	@Property(key = "gameserver.instance.cooldown_rate.excluded_maps", defaultValue = "")
	public static Set<Integer> INSTANCE_COOLDOWN_RATE_EXCLUDED_MAPS;

	@Property(key = "gameserver.instance.destroy_delay_seconds", defaultValue = "600")
	public static int INSTANCE_DESTROY_DELAY_SECONDS;

	@Property(key = "gameserver.instance.solo.destroy_delay_seconds", defaultValue = "600")
	public static int SOLO_INSTANCE_DESTROY_DELAY_SECONDS;

	@Property(key = "gameserver.instance.duel.enable", defaultValue = "true")
	public static boolean INSTANCE_DUEL_ENABLE;

	/**
	 * Location of instance *.java handlers
	 */
	@Property(key = "gameserver.instance.handler_directory", defaultValue = "./data/handlers/instance")
	public static File HANDLER_DIRECTORY;
}
