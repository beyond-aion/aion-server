package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author Tiger0319
 */
public class DropConfig {

	/**
	 * Disable drop rate reduction based on level difference between players and mobs
	 */
	@Property(key = "gameserver.drop.reduction.disable", defaultValue = "false")
	public static boolean DISABLE_DROP_REDUCTION;

	/**
	 * Enable announce when a player drops Unique / Epic item
	 */
	@Property(key = "gameserver.unique.drop.announce.enable", defaultValue = "true")
	public static boolean ENABLE_UNIQUE_DROP_ANNOUNCE;

	/**
	 * Disable drop rate reduction based on level difference in zone
	 */
	@Property(key = "gameserver.drop.noreduction", defaultValue = "0")
	public static String DISABLE_DROP_REDUCTION_IN_ZONES;
}
