package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author lord_rex
 * @modified Rolandas, changed formula which had no sense.
 */
public class ThreadConfig {

	/**
	 * Thread basepoolsize
	 */
	@Property(key = "gameserver.thread.basepoolsize", defaultValue = "1")
	public static int BASE_THREAD_POOL_SIZE;
	/**
	 * Thread threadpercore
	 */
	@Property(key = "gameserver.thread.threadpercore", defaultValue = "4")
	public static int EXTRA_THREAD_PER_CORE;
	/**
	 * Thread runtime
	 */
	@Property(key = "gameserver.thread.runtime", defaultValue = "5000")
	public static long MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING;

	/**
	 * For instant pool set priority to 7, in Linux you must be root and use extra switches
	 */
	@Property(key = "gameserver.thread.usepriority", defaultValue = "false")
	public static boolean USE_PRIORITIES;

	public static int THREAD_POOL_SIZE;

	public static void load() {
		final int extraThreadPerCore = EXTRA_THREAD_PER_CORE;

		THREAD_POOL_SIZE = (BASE_THREAD_POOL_SIZE + extraThreadPerCore) * Runtime.getRuntime().availableProcessors();
	}
}
