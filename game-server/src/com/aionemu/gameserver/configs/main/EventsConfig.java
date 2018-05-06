package com.aionemu.gameserver.configs.main;

import java.util.List;
import java.util.Set;

import com.aionemu.commons.configuration.Property;

/**
 * @author Rolandas
 */
public class EventsConfig {

	@Property(key = "gameserver.event.service.enabled_events")
	public static List<String> ENABLED_EVENTS;

	/**
	 * Event Arcade Upgrade
	 */
	@Property(key = "gameserver.event.arcade.enable", defaultValue = "false")
	public static boolean ENABLE_EVENT_ARCADE;

	@Property(key = "gameserver.event.arcade.resume_token", defaultValue = "3")
	public static int ARCADE_RESUME_TOKEN;

	/**
	 * Monster Raid
	 */
	@Property(key = "gameserver.monster.raid.enable", defaultValue = "false")
	public static boolean ENABLE_MONSTER_RAID;

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
