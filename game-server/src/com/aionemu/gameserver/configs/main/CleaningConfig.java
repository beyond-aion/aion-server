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
	 * Number of threads executing the cleaning If you have many chars to delete you should use a value between 4 and 6
	 */
	@Property(key = "gameserver.cleaning.threads", defaultValue = "2")
	public static int WORKER_THREADS;

	/**
	 * Maximum amount of accounts cleared at one execution (if too many chars are deleted in one run your database will get strongly fragmented which
	 * increases runtime dramatically)
	 */
	@Property(key = "gameserver.cleaning.account_limit", defaultValue = "500")
	public static int TOTAL_ACC_LIMIT;

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
