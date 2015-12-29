package com.aionemu.gameserver.configs.main;

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
	 * Siege sield Enabled
	 */
	@Property(key = "gameserver.siege.shield.enable", defaultValue = "true")
	public static boolean SIEGE_SHIELD_ENABLED;
	/**
	 * Balaur Assaults Enabled
	 */
	@Property(key = "gameserver.siege.assault.enable", defaultValue = "false")
	public static boolean BALAUR_AUTO_ASSAULT;
	@Property(key = "gameserver.siege.assault.rate", defaultValue = "1")
	public static float BALAUR_ASSAULT_RATE;

	/**
	 * Siege Race Protector spawn shedule
	 */
	@Property(key = "gameserver.siege.protector.time", defaultValue = "0 0 21 ? * *")
	public static String RACE_PROTECTOR_SPAWN_SCHEDULE;
	/**
	 * Berserker Sunayaka spawn time
	 */
	@Property(key = "gameserver.sunayaka.time", defaultValue = "0 0 23 ? * *")
	public static String BERSERKER_SUNAYAKA_SPAWN_SCHEDULE;
	/**
	 * Berserker Sunayaka spawn time
	 */
	@Property(key = "gameserver.moltenus.time", defaultValue = "0 0 22 ? * SUN")
	public static String MOLTENUS_SPAWN_SCHEDULE;
	/**
	 * Legendary npc's health multiplier
	 */
	@Property(key = "gameserver.siege.health.multiplier", defaultValue = "1")
	public static float SIEGE_HEALTH_MULTIPLIER;
	
	@Property(key ="gameserver.siege.panesterra.ahserion.enable", defaultValue="true")
	public static boolean AHSERION_ENABLED = false;
	
	@Property(key ="gameserver.siege.panesterra.ahserion.minplayer.type1", defaultValue="12")
	public static int AHSERION_MIN_PLAYERS_TEAM_TYPE_1 = 12;
	
	@Property(key ="gameserver.siege.panesterra.ahserion.maxplayer.type1", defaultValue="24")
	public static int AHSERION_MAX_PLAYERS_TEAM_TYPE_1 = 24;
	
	@Property(key ="gameserver.siege.panesterra.ahserion.teamType", defaultValue="2")
	public static byte AHSERION_TEAM_TYPE = 2;
}
