package com.aionemu.gameserver.configs.main;

import java.io.File;

import com.aionemu.commons.configuration.Property;

/**
 * @author ATracer
 */
public class WorldConfig {

	/**
	 * World region size
	 */
	@Property(key = "gameserver.world.region.size", defaultValue = "128")
	public static int WORLD_REGION_SIZE;

	@Property(key = "gameserver.world.max.twincount.usual", defaultValue = "1")
	public static int WORLD_MAX_TWINS_USUAL;

	/**
	 * Location of zone *.java handlers
	 */
	@Property(key = "gameserver.world.zone_handler_directory", defaultValue = "./data/handlers/zone")
	public static File ZONE_HANDLER_DIRECTORY;
}
