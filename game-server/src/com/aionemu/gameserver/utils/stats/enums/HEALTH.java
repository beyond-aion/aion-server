package com.aionemu.gameserver.utils.stats.enums;

/**
 * @author ATracer
 */
public enum HEALTH {
	WARRIOR(110),
	GLADIATOR(115),
	TEMPLAR(100),
	SCOUT(100),
	ASSASSIN(100),
	RANGER(90),
	MAGE(90),
	SORCERER(90),
	SPIRIT_MASTER(90),
	PRIEST(95),
	CLERIC(110),
	CHANTER(105),
	ENGINEER(100),
	GUNNER(100),
	RIDER(100),
	ARTIST(100),
	BARD(100);

	private int value;

	private HEALTH(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
