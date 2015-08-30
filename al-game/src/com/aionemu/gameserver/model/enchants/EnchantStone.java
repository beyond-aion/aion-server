package com.aionemu.gameserver.model.enchants;

import com.aionemu.commons.utils.Rnd;


/**
 * @author Whoop
 *
 */
public enum EnchantStone {
	
	ALPHA(1, 29),
	BETA(30, 59),
	GAMMA(60, 84),
	DELTA(85, 104),
	EPSILON(105, 120),
	OMEGA(121, 150);
	
	private int minLevel;
	private int maxLevel;
	
	private EnchantStone(int minLevel, int maxLevel) {
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
	}
	
	public int getMinLevel() {
		return this.minLevel;
	}
	
	public int getMaxLevel() {
		return this.maxLevel;
	}
	
	public int getRndLevel() {
		return Rnd.get(this.minLevel, this.maxLevel);
	}
}
