package com.aionemu.gameserver.model;

import java.util.NoSuchElementException;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;

/**
 * @author synchro2
 */
public enum SellLimit {

	LIMIT_1_30(1, 30, 5300047),
	LIMIT_31_40(31, 40, 7100047),
	LIMIT_41_55(41, 55, 12050047),
	LIMIT_56_60(56, 60, 14600047),
	LIMIT_61_65(61, 65, 17150047);

	private final int playerMinLevel;
	private final int playerMaxLevel;
	private final long limit;

	private SellLimit(int playerMinLevel, int playerMaxLevel, long limit) {
		this.playerMinLevel = playerMinLevel;
		this.playerMaxLevel = playerMaxLevel;
		this.limit = limit;
	}

	public static long getSellLimit(Player player) {
		int playerLevel = player.getAccount().getMaxPlayerLevel();
		for (SellLimit sellLimit : values()) {
			if (sellLimit.playerMinLevel <= playerLevel && sellLimit.playerMaxLevel >= playerLevel) {
				return Rates.SELL_LIMIT.calcResult(player, sellLimit.limit);
			}
		}
		throw new NoSuchElementException("Sell limit for player level: " + playerLevel + " was not found");
	}
}
