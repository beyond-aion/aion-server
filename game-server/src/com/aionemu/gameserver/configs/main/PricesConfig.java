package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author Sarynth
 */
public class PricesConfig {

	/**
	 * Controls the "Prices:" value in influence tab.
	 */
	@Property(key = "gameserver.prices.default.prices", defaultValue = "100")
	public static int DEFAULT_PRICES;

	/**
	 * Hidden modifier for all prices.
	 */
	@Property(key = "gameserver.prices.default.modifier", defaultValue = "100")
	public static int DEFAULT_MODIFIER;

	/**
	 * Taxes: value = 100 + tax %
	 */
	@Property(key = "gameserver.prices.default.taxes", defaultValue = "100")
	public static int DEFAULT_TAXES;

	@Property(key = "gameserver.prices.vendor.buymod", defaultValue = "100")
	public static int VENDOR_BUY_MODIFIER;

	@Property(key = "gameserver.prices.vendor.sellmod", defaultValue = "20")
	public static int VENDOR_SELL_MODIFIER;
}
