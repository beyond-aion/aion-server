package com.aionemu.gameserver.model.stats.calc;

import com.aionemu.gameserver.model.PlayerClass;

/**
 * @author Yeats
 */
public class PlayerStatCalculator {

	public static int calculateMaxHp(PlayerClass playerClass, int level) {
		int base = playerClass.getHealthMultiplier() / 2;
		float mod1 = 0.1075f * playerClass.getHealthMultiplier();
		float mod2 = 0.002875f * playerClass.getHealthMultiplier();
		return (int) (base + level * mod1 + level * level * mod2);
	}

	public static int calculateMaxMp(PlayerClass playerClass, int level) {
		float base = playerClass.getWillMultiplier() * 0.35f;
		float mod1 = level * base / 2f;
		float mod2 = level * level * playerClass.getWillMultiplier() * 0.125f / 10000;
		return (int) (base + mod1 + mod2);
	}

	public static int calculateBlockEvasionOrParry(int level) {
		return (int) (62 + 12.4f * level);
	}

	public static int calculateMagicalAccuracy(int level) {
		return (int) (14.26f * level);
	}

	public static int calculatePhysicalAccuracy(int level) {
		return 190 + 8 * level;
	}

	public static int calculateStrikeResist(int level) {
		return level > 50 ? 6 * (level - 50) : 0;
	}
}
