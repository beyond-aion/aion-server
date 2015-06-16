package com.aionemu.gameserver.utils.stats.enums;

/**
 * @author ATracer
 */
public enum FLY_SPEED {
	WARRIOR(9),
	GLADIATOR(9),
	TEMPLAR(9),
	SCOUT(9),
	ASSASSIN(9),
	RANGER(9),
	MAGE(9),
	SORCERER(9),
	SPIRIT_MASTER(9),
	PRIEST(9),
	CLERIC(9),
	CHANTER(9),
	ENGINEER(9),
	GUNNER(9),
	RIDER(9),
	ARTIST(9),
	BARD(9);

	private int value;

	private FLY_SPEED(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
