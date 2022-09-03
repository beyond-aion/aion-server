package com.aionemu.gameserver.configs.main;

import org.quartz.CronExpression;

import com.aionemu.commons.configuration.Property;

/**
 * @author xTz
 */
public class AutoGroupConfig {

	@Property(key = "gameserver.autogroup.enable", defaultValue = "true")
	public static boolean AUTO_GROUP_ENABLE;

	@Property(key = "gameserver.startTime.enable", defaultValue = "true")
	public static boolean START_TIME_ENABLE;

	@Property(key = "gameserver.dredgion.registration_period", defaultValue = "60")
	public static long DREDGION_REGISTRATION_PERIOD;

	@Property(key = "gameserver.dredgion.time", defaultValue = "\"0 0 0,12,20 ? * *\"")
	public static CronExpression[] DREDGION_TIMES;

	@Property(key = "gameserver.kamar_battlefield.registration_period", defaultValue = "60")
	public static long KAMAR_BATTLEFIELD_REGISTRATION_PERIOD;

	@Property(key = "gameserver.kamar_battlefield.time", defaultValue = "\"0 0 0,20 ? * MON,WED,SAT\"")
	public static CronExpression[] KAMAR_BATTLEFIELD_TIMES;

	@Property(key = "gameserver.engulfed_ophidan_bridge.registration_period", defaultValue = "60")
	public static long ENGULFED_OPHIDAN_BRIDGE_REGISTRATION_PERIOD;

	@Property(key = "gameserver.engulfed_ophidan_bridge.time", defaultValue = "\"0 0 12,19 ? * *\"")
	public static CronExpression[] ENGULFED_OPHIDAN_BRIDGE_TIMES;

	@Property(key = "gameserver.iron_wall_warfront.registration_period", defaultValue = "60")
	public static long IRON_WALL_WARFRONT_REGISTRATION_PERIOD;

	@Property(key = "gameserver.iron_wall_warfront.time", defaultValue = "\"0 0 0,12 ? * SUN\"")
	public static CronExpression[] IRON_WALL_WARFRONT_TIMES;

	@Property(key = "gameserver.idgel_dome.registration_period", defaultValue = "60")
	public static long IDGEL_DOME_REGISTRATION_PERIOD;

	@Property(key = "gameserver.idgel_dome.time", defaultValue = "0 0 23 ? * *")
	public static CronExpression[] IDGEL_DOME_TIMES;

	@Property(key = "gameserver.autogroup.announce_battleground_registrations", defaultValue = "false")
	public static boolean ANNOUNCE_BATTLEGROUND_REGISTRATIONS;
}
