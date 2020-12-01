package com.aionemu.gameserver.configs.main;

import java.util.Set;

import com.aionemu.commons.configuration.Property;

/**
 * @author Rolandas
 */
public class EventsConfig {

	@Property(key = "gameserver.event.service.disabled_events")
	public static Set<String> DISABLED_EVENTS;

	/**
	 * Event Upgrade Arcade
	 */
	@Property(key = "gameserver.event.arcade.enable", defaultValue = "false")
	public static boolean ENABLE_EVENT_ARCADE;

	@Property(key = "gameserver.event.arcade.resume_token", defaultValue = "3")
	public static int ARCADE_RESUME_TOKEN;

	/**
	 * World Raid
	 */
	@Property(key = "gameserver.worldraid.enable", defaultValue = "true")
	public static boolean ENABLE_WORLDRAID;

	@Property(key = "gameserver.worldraid.use_spawn_msg", defaultValue = "true")
	public static boolean WORLDRAID_ENABLE_SPAWNMSG;

	/**
	 * Headhunting
	 */
	@Property(key = "gameserver.event.headhunting.enable", defaultValue = "false")
	public static boolean ENABLE_HEADHUNTING;

	@Property(key = "gameserver.event.headhunting.maps", defaultValue = "")
	public static Set<Integer> HEADHUNTING_MAPS;

	@Property(key = "gameserver.event.headhunting.consolation_prize_kills", defaultValue = "50")
	public static int HEADHUNTING_CONSOLATION_PRIZE_KILLS;
}
