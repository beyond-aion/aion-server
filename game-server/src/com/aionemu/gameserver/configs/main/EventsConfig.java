package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author Rolandas
 */
public class EventsConfig {

	/**
	 * Event Enabled
	 */
	@Property(key = "gameserver.event.enable", defaultValue = "false")
	public static boolean EVENT_ENABLED;

	/**
	 * Event Rewarding Membership
	 */
	@Property(key = "gameserver.event.membership", defaultValue = "0")
	public static int EVENT_REWARD_MEMBERSHIP;

	@Property(key = "gameserver.event.membership.rate", defaultValue = "false")
	public static boolean EVENT_REWARD_MEMBERSHIP_RATE;

	/**
	 * Event Rewarding Period
	 */
	@Property(key = "gameserver.event.period", defaultValue = "60")
	public static int EVENT_PERIOD;

	/**
	 * Event Reward Values
	 */
	@Property(key = "gameserver.event.item.elyos", defaultValue = "141000001")
	public static int EVENT_ITEM_ELYOS;

	@Property(key = "gameserver.event.item.asmo", defaultValue = "141000001")
	public static int EVENT_ITEM_ASMO;

	@Property(key = "gameserver.events.givejuice", defaultValue = "160009017")
	public static int EVENT_GIVEJUICE;

	@Property(key = "gameserver.events.givecake", defaultValue = "160010073")
	public static int EVENT_GIVECAKE;

	@Property(key = "gameserver.event.count", defaultValue = "1")
	public static int EVENT_ITEM_COUNT;

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

	/**
	 * Monster Raid
	 */
	@Property(key = "gameserver.monster.raid.enable", defaultValue = "false")
	public static boolean ENABLE_MONSTER_RAID;
}
