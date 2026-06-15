package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author nrg
 */
public class CleaningConfig {

	/**
	 * Enable Database Cleaning
	 */
	@Property(key = "gameserver.cleaning.enable", defaultValue = "false")
	public static boolean CLEANING_ENABLE;

	/**
	 * Minimum account inactivity in days, after which chars get deleted<br>
	 * Cleaning will only be executed with a value greater than 30
	 */
	@Property(key = "gameserver.cleaning.min_account_inactivity", defaultValue = "365")
	public static int MIN_ACCOUNT_INACTIVITY_DAYS;

	/**
	 * Maximum level of characters that will be deleted on each account
	 */
	@Property(key = "gameserver.cleaning.max_level", defaultValue = "25")
	public static int MAX_DELETABLE_CHAR_LEVEL;
}
