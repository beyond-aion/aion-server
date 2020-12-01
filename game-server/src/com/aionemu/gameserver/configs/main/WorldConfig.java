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

	@Property(key = "gameserver.world.max.twincount.beginner", defaultValue = "-1")
	public static int WORLD_MAX_TWINS_BEGINNER;

	@Property(key = "gameserver.world.emulate.fasttrack", defaultValue = "true")
	public static boolean WORLD_EMULATE_FASTTRACK;

	/**
	 * Location of zone *.java handlers
	 */
	@Property(key = "gameserver.world.zone_handler_directory", defaultValue = "./data/handlers/zone")
	public static File ZONE_HANDLER_DIRECTORY;
}
