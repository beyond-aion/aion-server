package com.aionemu.gameserver.configs.main;

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

	/**
	 * Trace active regions and deactivate inactive
	 */
	@Property(key = "gameserver.world.region.active.trace", defaultValue = "true")
	public static boolean WORLD_ACTIVE_TRACE;

	@Property(key = "gameserver.world.max.twincount.usual", defaultValue = "1")
	public static int WORLD_MAX_TWINS_USUAL;

	@Property(key = "gameserver.world.max.twincount.beginner", defaultValue = "-1")
	public static int WORLD_MAX_TWINS_BEGINNER;
	
	@Property(key = "gameserver.world.emulate.fasttrack", defaultValue = "true")
	public static boolean WORLD_EMULATE_FASTTRACK;

	@Property(key = "gameserver.world.specialzone.shownames", defaultValue = "true")
	public static boolean ENABLE_SHOW_ZONEENTER;
}
