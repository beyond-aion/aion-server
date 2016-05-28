package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author Rolandas
 */
public class EventsConfig {

	@Property(key = "gameserver.event.service.enable", defaultValue = "false")
	public static boolean ENABLE_EVENT_SERVICE;

	@Property(key = "gameserver.event.service.enabled_events", defaultValue = "")
	public static String ENABLED_EVENTS;

	/**
	 * Event Arcade Upgrade
	 */
	@Property(key = "gameserver.event.arcade.enable", defaultValue = "false")
	public static boolean ENABLE_EVENT_ARCADE;

	@Property(key = "gameserver.event.arcade.chance", defaultValue = "50")
	public static int EVENT_ARCADE_CHANCE;
	
	@Property(key = "gameserver.event.arcade.resume_token", defaultValue = "3")
	public static int ARCADE_RESUME_TOKEN;

	/**
	 * Monster Raid
	 */
	@Property(key = "gameserver.monster.raid.enable", defaultValue = "false")
	public static boolean ENABLE_MONSTER_RAID;
}
