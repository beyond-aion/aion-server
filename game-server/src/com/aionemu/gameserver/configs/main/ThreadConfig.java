package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author lord_rex, Neon
 */
public class ThreadConfig {

	@Property(key = "gameserver.thread.base_pool_size", defaultValue = "0")
	public static int BASE_THREAD_POOL_SIZE;

	@Property(key = "gameserver.thread.scheduled_pool_size", defaultValue = "0")
	public static int SCHEDULED_THREAD_POOL_SIZE;

	@Property(key = "gameserver.thread.runtime", defaultValue = "5000")
	public static long MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING;

	/**
	 * For instant pool set priority to 7, in Linux you must be root and use extra switches
	 */
	@Property(key = "gameserver.thread.usepriority", defaultValue = "false")
	public static boolean USE_PRIORITIES;
}
