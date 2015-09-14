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
	 * Period after which inactive chars get deleted It is expressed in number of days
	 */
	@Property(key = "gameserver.cleaning.period", defaultValue = "180")
	public static int CLEANING_PERIOD;

	/**
	 * Number of threads executing the cleaning If you have many chars to delete you should use a value between 4 and 6
	 */
	@Property(key = "gameserver.cleaning.threads", defaultValue = "2")
	public static int CLEANING_THREADS;

	/**
	 * Maximum amount of accounts cleared at one execution If too many chars are deleted in one run your database will get strongly fragmented which
	 * increases runtime dramatically Note: 0 for not limitation
	 */
	@Property(key = "gameserver.cleaning.limit", defaultValue = "600")
	public static int CLEANING_LIMIT;
}
