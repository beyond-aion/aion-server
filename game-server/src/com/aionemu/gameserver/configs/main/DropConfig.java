package com.aionemu.gameserver.configs.main;

import java.util.List;

import com.aionemu.commons.configuration.Property;
import com.aionemu.gameserver.model.templates.item.ItemQuality;

/**
 * @author Tiger0319
 * @reworked Neon
 */
public class DropConfig {

	/**
	 * Disable drop rate reduction based on level difference between players and mobs
	 */
	@Property(key = "gameserver.drop.disable_reduction", defaultValue = "false")
	public static boolean DISABLE_REDUCTION;

	/**
	 * Announce when a player drops an item with the configured minimum item quality
	 * @see ItemQuality
	 */
	@Property(key = "gameserver.drop.announce_quality")
	public static ItemQuality MIN_ANNOUNCE_QUALITY;

	/**
	 * Disable drop rate reduction based on level difference for maps
	 */
	@Property(key = "gameserver.drop.no_reduction_maps")
	public static List<Integer> NO_REDUCTION_MAPS;
	
	/**
	 * Disable range checks for specified maps
	 */
	@Property(key = "gameserver.drop.disable_range_check_maps")
	public static List<Integer> DISABLE_RANGE_CHECK_MAPS;
}
