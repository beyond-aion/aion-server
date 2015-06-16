package com.aionemu.gameserver.utils.stats.enums;

/**
 * @author ATracer
 */
public enum MAIN_HAND_CRITRATE {
	WARRIOR(2),
	GLADIATOR(2),
	TEMPLAR(2),
	SCOUT(3),
	ASSASSIN(3),
	RANGER(3),
	MAGE(1),
	SORCERER(2),
	SPIRIT_MASTER(2),
	PRIEST(2),
	CLERIC(2),
	CHANTER(1),
	ENGINEER(1),
	GUNNER(1),
	RIDER(1),
	ARTIST(1),
	BARD(1);

	private int value;

	private MAIN_HAND_CRITRATE(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
