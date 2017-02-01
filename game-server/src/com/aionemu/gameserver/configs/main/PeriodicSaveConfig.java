package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author ATracer
 */
public class PeriodicSaveConfig {

	/**
	 * Time in seconds for saving player data
	 */
	@Property(key = "gameserver.periodicsave.player.general", defaultValue = "900")
	public static int PLAYER_GENERAL;

	/**
	 * Time in seconds for saving player items and item stones
	 */
	@Property(key = "gameserver.periodicsave.player.items", defaultValue = "900")
	public static int PLAYER_ITEMS;

	/**
	 * Time in seconds for saving legion wh items and item stones
	 */
	@Property(key = "gameserver.periodicsave.legion.items", defaultValue = "1200")
	public static int LEGION_ITEMS;

	/**
	 * Time in seconds for updating and saving pet mood data
	 */
	@Property(key = "gameserver.periodicsave.player.pets", defaultValue = "10")
	public static int PLAYER_PETS;
}
