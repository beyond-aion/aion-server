package com.aionemu.gameserver.utils.stats.enums;

/**
 * @author ATracer
 */
public enum SPEED {
	WARRIOR(6),
	GLADIATOR(6),
	TEMPLAR(6),
	SCOUT(6),
	ASSASSIN(6),
	RANGER(6),
	MAGE(6),
	SORCERER(6),
	SPIRIT_MASTER(6),
	PRIEST(6),
	CLERIC(6),
	CHANTER(6),
	ENGINEER(6),
	GUNNER(6),
	RIDER(6),
	ARTIST(6),
	BARD(6);

	private int value;

	private SPEED(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
