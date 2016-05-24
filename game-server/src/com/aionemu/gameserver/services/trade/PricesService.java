package com.aionemu.gameserver.services.trade;

import com.aionemu.gameserver.configs.main.PricesConfig;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.siege.Influence;

/**
 * @author Sarynth modified by wakizashi Used to get prices for the player. - Packets: SM_PRICES, SM_TRADELIST, SM_SELL_ITEM - Services: Godstone
 *         socket, teleporter, other fees. TODO: Add Player owner; value and check for PremiumRates or faction price influence.
 */
public class PricesService {

	/**
	 * Used in SM_PRICES
	 * 
	 * @return buyingPrice
	 */
	public static final int getGlobalPrices(Race playerRace) {
		int defaultPrices = PricesConfig.DEFAULT_PRICES;

		if (!SiegeConfig.SIEGE_ENABLED)
			return defaultPrices;

		float influenceValue = 0;
		switch (playerRace) {
			case ASMODIANS:
				influenceValue = Influence.getInstance().getGlobalAsmodiansInfluence();
				break;
			case ELYOS:
				influenceValue = Influence.getInstance().getGlobalElyosInfluence();
				break;
			default:
				influenceValue = 0.5f;
				break;
		}
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
	 * 
	 * @return
	 */
	public static final int getGlobalPricesModifier() {
		return PricesConfig.DEFAULT_MODIFIER;
	}

	/**
	 * Used in SM_PRICES
	 * 
	 * @return taxes
	 */
	public static final int getTaxes(Race playerRace) {
		int defaultTax = PricesConfig.DEFAULT_TAXES;

		if (!SiegeConfig.SIEGE_ENABLED)
			return defaultTax;

		float influenceValue = 0;
		switch (playerRace) {
			case ASMODIANS:
				influenceValue = Influence.getInstance().getGlobalAsmodiansInfluence();
				break;
			case ELYOS:
				influenceValue = Influence.getInstance().getGlobalElyosInfluence();
				break;
			default:
				influenceValue = 0.5f;
				break;
		}
		if (influenceValue >= 0.5f) {
			return defaultTax;
		}
		float diff = 0.5f - influenceValue;
		return Math.round(defaultTax + ((diff / 4) * 100));
	}

	/**
	 * Used in SM_TRADELIST.
	 * 
	 * @return buyPriceModifier
	 */
	public static final int getVendorBuyModifier() {
		return PricesConfig.VENDOR_BUY_MODIFIER;
	}

	/**
	 * Used in SM_SELL_ITEM
	 * 
	 * @return The default sellModifier, but some npcs and merchant pets use their own values.
	 */
	public static final int getVendorSellModifier() {
		return PricesConfig.VENDOR_SELL_MODIFIER;
	}

	/**
	 * @return The calculated price after taxes and global modifiers.
	 */
	public static final long getPriceForService(long basePrice, Race playerRace) {
		// Tricky. Requires multiplication by Prices, Modifier, Taxes
		// In order, and round down each time to match client calculation.
		return (long) ((long) ((long) (basePrice * getGlobalPrices(playerRace) / 100D) * getGlobalPricesModifier() / 100D) * getTaxes(playerRace) / 100D);
	}

	/**
	 * @return The calculated price after taxes, vendor and global modifiers.
	 */
	public static final long getBuyPrice(long requiredKinah, Race playerRace) {
		// Requires double precision for 2mil+ kinah items
		return (long) ((long) ((long) ((long) (requiredKinah * getVendorBuyModifier() / 100D) * getGlobalPrices(playerRace) / 100D)
			* getGlobalPricesModifier() / 100D) * getTaxes(playerRace) / 100D);
	}

	/**
	 * @return The calculated Kinah reward after applying sellModifier (default would be 20 = 20% of the original value, see {@link #getVendorSellModifier()}).
	 */
	public static final long getSellReward(long kinahValue, int sellModifier) {
		return (long) (kinahValue * sellModifier / 100D);
	}
}
