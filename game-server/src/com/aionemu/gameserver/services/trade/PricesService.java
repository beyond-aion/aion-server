package com.aionemu.gameserver.services.trade;

import com.aionemu.gameserver.configs.main.PricesConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.siege.Influence;

/**
 * Used to get effective prices for the player.<br/>
 * Packets: SM_PRICES, SM_TRADELIST, SM_SELL_ITEM<br/>
 * Services: Teleporter and similar fees
 * 
 * @author Sarynth, wakizashi
 */
public class PricesService {

	/**
	 * Used in SM_PRICES
	 * 
	 * @return buyingPrice
	 */
	public static int getGlobalPrices(Race playerRace) {
		int defaultPrices = PricesConfig.DEFAULT_PRICES;
		float influenceValue = getPriceInfluenceRate(playerRace);
		if (influenceValue == 0.5f) {
			return defaultPrices;
		} else if (influenceValue > 0.5f) {
			float diff = influenceValue - 0.5f;
			return Math.round(defaultPrices - ((diff / 2) * 100));
		} else {
			float diff = 0.5f - influenceValue;
			return Math.round(defaultPrices + ((diff / 2) * 100));
		}
	}

	/**
	 * Used in SM_PRICES
	 */
	public static int getGlobalPricesModifier() {
		return PricesConfig.DEFAULT_MODIFIER;
	}

	/**
	 * Used in SM_PRICES
	 */
	public static int getTaxes(Race playerRace) {
		int defaultTax = PricesConfig.DEFAULT_TAXES;
		float influenceValue = getPriceInfluenceRate(playerRace);
		if (influenceValue >= 0.5f) {
			return defaultTax;
		}
		float diff = 0.5f - influenceValue;
		return Math.round(defaultTax + ((diff / 4) * 100));
	}

	private static float getPriceInfluenceRate(Race playerRace) {
		switch (playerRace) {
			case ASMODIANS:
				return Influence.getInstance().getAsmodianInfluenceRate();
			case ELYOS:
				return Influence.getInstance().getElyosInfluenceRate();
		}
		throw new IllegalArgumentException(playerRace + " is no valid player race.");
	}

	/**
	 * Used in SM_TRADELIST.
	 * 
	 * @return buyPriceModifier
	 */
	public static int getVendorBuyModifier() {
		return PricesConfig.VENDOR_BUY_MODIFIER;
	}

	/**
	 * Used in SM_SELL_ITEM
	 * 
	 * @return The default sellModifier, but some npcs and merchant pets use their own values.
	 */
	public static int getVendorSellModifier() {
		return PricesConfig.VENDOR_SELL_MODIFIER;
	}

	/**
	 * @return The calculated price after taxes and global modifiers.
	 */
	public static long getPriceForService(long basePrice, Race playerRace) {
		// Tricky. Requires multiplication by Prices, Modifier, Taxes
		// In order, and round down each time to match client calculation.
		return (long) ((long) ((long) (basePrice * getGlobalPrices(playerRace) / 100D) * getGlobalPricesModifier() / 100D) * getTaxes(playerRace) / 100D);
	}

	/**
	 * @return The calculated price after taxes, vendor and global modifiers.
	 */
	public static long getBuyPrice(long requiredKinah, Race playerRace) {
		// Requires double precision for 2mil+ kinah items
		return (long) ((long) ((long) ((long) (requiredKinah * getVendorBuyModifier() / 100D) * getGlobalPrices(playerRace) / 100D)
			* getGlobalPricesModifier() / 100D) * getTaxes(playerRace) / 100D);
	}

	/**
	 * @return The calculated Kinah reward after applying sellModifier (default would be 20 = 20% of the original value, see {@link #getVendorSellModifier()}).
	 */
	public static long getSellReward(long kinahValue, int sellModifier) {
		return (long) (kinahValue * sellModifier / 100D);
	}
}
