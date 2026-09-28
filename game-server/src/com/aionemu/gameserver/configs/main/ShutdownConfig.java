package com.aionemu.gameserver.configs.main;

import org.quartz.CronExpression;

import com.aionemu.commons.configuration.Property;

/**
 * @author lord_rex
 */
public class ShutdownConfig {

	/**
	 * Shutdown Hook delay in seconds.
	 */
	@Property(key = "gameserver.shutdown.delay", defaultValue = "120")
	public static int DELAY;

	/**
	 * Shut down instantly if there are no other players online except staff.
	 */
	@Property(key = "gameserver.shutdown.instant_shutdown_with_only_staff_online", defaultValue = "true")
	public static boolean INSTANT_SHUTDOWN_WITH_ONLY_STAFF_ONLINE;

	/**
	 * Shutdown restart schedule.
	 */
	@Property(key = "gameserver.shutdown.restart_schedule")
	public static CronExpression RESTART_SCHEDULE;

}
