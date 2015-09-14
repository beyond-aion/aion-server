package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author xTz
 */
public class InGameShopConfig {

	/**
	 * Enable in game shop
	 */
	@Property(key = "gameserver.ingameshop.enable", defaultValue = "false")
	public static boolean ENABLE_IN_GAME_SHOP;

	/**
	 * Enable gift system between factions
	 */
	@Property(key = "gameserver.ingameshop.gift", defaultValue = "false")
	public static boolean ENABLE_GIFT_OTHER_RACE;

	@Property(key = "gameserver.ingameshop.allow.gift", defaultValue = "true")
	public static boolean ALLOW_GIFTS;

	@Property(key = "webshop.enabled", defaultValue = "false")
	public static boolean WEBSHOP_ENABLED;

	@Property(key = "webshop.update.frequency", defaultValue = "3000")
	public static int WEBSHOP_UPDATE_FREQUENCY;
}
