package com.aionemu.gameserver.utils.stats.enums;

/**
 * @author ATracer
 */
public enum MAIN_HAND_ATTACK {
	WARRIOR(19),
	GLADIATOR(19),
	TEMPLAR(19),
	SCOUT(18),
	ASSASSIN(19),
	RANGER(18),
	MAGE(16),
	SORCERER(16),
	SPIRIT_MASTER(16),
	PRIEST(17),
	CLERIC(19),
	CHANTER(19),
	ENGINEER(19),
	GUNNER(19),
	RIDER(19),
	ARTIST(19),
	BARD(19);

	private int value;

	private MAIN_HAND_ATTACK(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
