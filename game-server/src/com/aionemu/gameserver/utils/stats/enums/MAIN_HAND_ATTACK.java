package com.aionemu.gameserver.utils.stats.enums;

/**
 * @author ATracer
 */
public enum MAIN_HAND_ATTACK {
	WARRIOR(21),
	GLADIATOR(21),
	TEMPLAR(21),
	SCOUT(18),
	ASSASSIN(20),
	RANGER(18),
	MAGE(14),
	SORCERER(14),
	SPIRIT_MASTER(14),
	PRIEST(18),
	CLERIC(18),
	CHANTER(20),
	ENGINEER(18),
	GUNNER(18),
	RIDER(20),
	ARTIST(14),
	BARD(14);

	private int value;

	private MAIN_HAND_ATTACK(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
