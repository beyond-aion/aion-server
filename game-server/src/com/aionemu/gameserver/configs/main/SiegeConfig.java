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
	/**
	 * Legendary npc's health multiplier
	 */
	@Property(key = "gameserver.siege.health.multiplier", defaultValue = "1")
	public static float SIEGE_HEALTH_MULTIPLIER;

	@Property(key = "gameserver.siege.panesterra.ahserion.maxplayers", defaultValue = "100")
	public static int AHSERION_MAX_PLAYERS_PER_TEAM;

	@Property(key = "gameserver.siege.panesterra.ahserion.time", defaultValue = "0 50 18 ? * SUN")
	public static CronExpression AHSERION_START_SCHEDULE;

	@Property(key = "gameserver.siege.legion.gp.cap_per_member", defaultValue = "200")
	public static int LEGION_GP_CAP_PER_MEMBER;
}
