package com.aionemu.gameserver.model.enchants;

import com.aionemu.gameserver.model.templates.item.ItemQuality;

/**
 * @author Neon
 */
public enum EnchantmentStone {

	ALPHA(20, ItemQuality.RARE),
	BETA(40, ItemQuality.LEGEND),
	GAMMA(55, ItemQuality.UNIQUE),
	DELTA(60, ItemQuality.EPIC),
	EPSILON(65, ItemQuality.MYTHIC),
	OMEGA(65, ItemQuality.MYTHIC);

	private final int baseLevel;
	private final ItemQuality baseQuality;

	private EnchantmentStone(int baseLevel, ItemQuality baseQuality) {
		this.baseLevel = baseLevel;
		this.baseQuality = baseQuality;
	}

	public int getBaseLevel() {
		return baseLevel;
	}

	public ItemQuality getBaseQuality() {
		return baseQuality;
	}

	public static EnchantmentStone getByItemId(int itemId) {
		switch (itemId) {
			case 166000191:
				return ALPHA;
			case 166000192:
				return BETA;
			case 166000193:
				return GAMMA;
			case 166000194:
				return DELTA;
			case 166000195:
				return EPSILON;
			case 166020000:
			case 166020001:
			case 166020002:
			case 166020003:
				return OMEGA;
			default:
				if (itemId >= 166000001 && itemId <= 166000190) { // L1 - L190 (old stones)
					if (itemId > 166000100) { // 101+
						return EPSILON;
					} else if (itemId > 166000060) { // 61-100
						return DELTA;
					} else if (itemId > 166000050) { // 51-60
						return GAMMA;
					} else if (itemId > 166000030) { // 31-50
						return BETA;
					} else { // 1-30
						return ALPHA;
					}
				}
				throw new IllegalArgumentException("No matching enchantment stone found for item ID " + itemId);
		}
	}
}
