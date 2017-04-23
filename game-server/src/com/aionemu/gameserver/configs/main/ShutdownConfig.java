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
	 * Shutdown restart schedule.
	 */
	@Property(key = "gameserver.shutdown.restart_schedule")
	public static CronExpression RESTART_SCHEDULE;

}
