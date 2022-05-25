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

	@Property(key = "gameserver.dredgion.enable", defaultValue = "true")
	public static boolean DREDGION_ENABLE;

	@Property(key = "gameserver.dredgion.timer", defaultValue = "120")
	public static long DREDGION_TIMER;

	@Property(key = "gameserver.dredgion.time", defaultValue = "\"0 0 0,12,20 ? * *\"")
	public static CronExpression[] DREDGION_TIMES;

	@Property(key = "gameserver.kamar.timer", defaultValue = "120")
	public static long KAMAR_TIMER;

	@Property(key = "gameserver.kamar.enable", defaultValue = "true")
	public static boolean KAMAR_ENABLE;

	@Property(key = "gameserver.kamar.time", defaultValue = "\"0 0 0,20 ? * MON,WED,SAT\"")
	public static CronExpression[] KAMAR_TIMES;

	@Property(key = "gameserver.engulfed_ob.timer", defaultValue = "120")
	public static long ENGULFED_OB_TIMER;

	@Property(key = "gameserver.engulfed_ob.enable", defaultValue = "true")
	public static boolean ENGULFED_OB_ENABLE;

	@Property(key = "gameserver.engulfed_ob.time", defaultValue = "\"0 0 12,19 ? * *\"")
	public static CronExpression[] ENGULFED_OB_TIMES;

	@Property(key = "gameserver.iron_wall_front.timer", defaultValue = "120")
	public static long IRON_WALL_FRONT_TIMER;

	@Property(key = "gameserver.iron_wall_front.enable", defaultValue = "true")
	public static boolean IRON_WALL_FRONT_ENABLE;

	@Property(key = "gameserver.iron_wall_front.time", defaultValue = "\"0 0 0,12 ? * SUN\"")
	public static CronExpression[] IRON_WALL_FRONT_TIMES;

	@Property(key = "gameserver.idgel_dome.timer", defaultValue = "60")
	public static long IDGEL_DOME_TIMER;

	@Property(key = "gameserver.idgel_dome.enable", defaultValue = "true")
	public static boolean IDGEL_DOME_ENABLE;

	@Property(key = "gameserver.idgel_dome.time", defaultValue = "0 0 23 ? * *")
	public static CronExpression[] IDGEL_DOME_TIMES;

	@Property(key = "gameserver.autogroup.announce_battleground_registrations", defaultValue = "false")
	public static boolean ANNOUNCE_BATTLEGROUND_REGISTRATIONS;
}
