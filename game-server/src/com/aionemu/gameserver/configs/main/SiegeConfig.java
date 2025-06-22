package com.aionemu.gameserver.configs.main;

import org.quartz.CronExpression;

import com.aionemu.commons.configuration.Property;

/**
 * @author Sarynth, xTz, Source
 */
public class SiegeConfig {

	/**
	 * Siege Enabled
	 */
	@Property(key = "gameserver.siege.enable", defaultValue = "true")
	public static boolean SIEGE_ENABLED;
	/**
	 * Balaur Assaults Enabled
	 */
	@Property(key = "gameserver.siege.assault.enable", defaultValue = "false")
	public static boolean BALAUR_AUTO_ASSAULT;
	@Property(key = "gameserver.siege.assault.rate", defaultValue = "1")
	public static float BALAUR_ASSAULT_RATE;

	/**
	 * Berserker Sunayaka spawn time
	 */
	@Property(key = "gameserver.moltenus.time", defaultValue = "0 0 22 ? * SUN")
	public static CronExpression MOLTENUS_SPAWN_SCHEDULE;

	@Property(key = "gameserver.siege.health.multiplier.fortress", defaultValue = "1")
	public static float FORTRESS_PROTECTOR_HEALTH_MULTIPLIER;

	@Property(key = "gameserver.siege.health.multiplier.artifact", defaultValue = "1")
	public static float ARTIFACT_PROTECTOR_HEALTH_MULTIPLIER;

	@Property(key = "gameserver.siege.health.multiplier.base", defaultValue = "1")
	public static float BASE_PROTECTOR_HEALTH_MULTIPLIER;

	@Property(key = "gameserver.siege.difficulty.multiplier", defaultValue = "1")
	public static float SIEGE_DIFFICULTY_MULTIPLIER;

	@Property(key = "gameserver.siege.panesterra.maxplayers", defaultValue = "100")
	public static int PANESTERRA_MAX_PLAYERS_PER_TEAM;

	@Property(key = "gameserver.siege.panesterra.ahserion.maxplayers", defaultValue = "100")
	public static int AHSERION_MAX_PLAYERS_PER_TEAM;

	@Property(key = "gameserver.siege.panesterra.ahserion.time", defaultValue = "0 50 18 ? * SUN")
	public static CronExpression AHSERION_START_SCHEDULE;

	@Property(key = "gameserver.siege.legion.gp.cap_per_member", defaultValue = "200")
	public static int LEGION_GP_CAP_PER_MEMBER;

	@Property(key = "gameserver.siege.door.repair.heal.percent", defaultValue = "0.01")
	public static double DOOR_REPAIR_HEAL_PERCENT;

	@Property(key = "gameserver.siege.reward.balaur.victory", defaultValue = "false")
	public static boolean SIEGE_REWARD_BALAUR_VICTORY;

	@Property(key = "gameserver.siege.ignore_staff_on_location_clear", defaultValue = "false")
	public static boolean IGNORE_STAFF_ON_LOCATION_CLEAR;

}
