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
	 * Legendary npc's health mod
	 */
	@Property(key = "gameserver.siege.health.mod", defaultValue = "false")
	public static boolean SIEGE_HEALTH_MOD_ENABLED;
	/**
	 * Legendary npc's health multiplier
	 */
	@Property(key = "gameserver.source.health.multiplier", defaultValue = "1.0")
	public static double SOURCE_HEALTH_MULTIPLIER = 1.0;
	/**
	 * Legendary npc's health mod
	 */
	@Property(key = "gameserver.source.health.mod", defaultValue = "false")
	public static boolean SOURCE_HEALTH_MOD_ENABLED;
	/**
	 * Legendary npc's health multiplier
	 */
	@Property(key = "gameserver.siege.health.multiplier", defaultValue = "1.0")
	public static double SIEGE_HEALTH_MULTIPLIER = 1.0;
	/**
	 * Tiamat's Incarnation dispell avatars
	 */
	@Property(key = "gameserver.siege.ida", defaultValue = "false")
	public static boolean SIEGE_IDA_ENABLED;
}
