package com.aionemu.gameserver.utils.stats.enums;

/**
 * @author ATracer
 */
public enum WILL {
	WARRIOR(90),
	GLADIATOR(90),
	TEMPLAR(105),
	SCOUT(90),
	ASSASSIN(90),
	RANGER(110),
	MAGE(115),
	SORCERER(110),
	SPIRIT_MASTER(115),
	PRIEST(110),
	CLERIC(110),
	CHANTER(110),
	ENGINEER(100),
	GUNNER(100),
	RIDER(100),
	ARTIST(100),
	BARD(100);

	private int value;

	private WILL(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
