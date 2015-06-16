package com.aionemu.gameserver.utils.stats.enums;

/**
 * @author ATracer
 */
public enum ATTACK_SPEED {
	WARRIOR(1500),
	GLADIATOR(1500),
	TEMPLAR(1500),
	SCOUT(1500),
	ASSASSIN(1500),
	RANGER(1500),
	MAGE(1500),
	SORCERER(1500),
	SPIRIT_MASTER(1500),
	PRIEST(1500),
	CLERIC(1500),
	CHANTER(1500),
	ENGINEER(1500),
	GUNNER(1500),
	RIDER(1500),
	ARTIST(1500),
	BARD(1500);

	private int value;

	private ATTACK_SPEED(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}
}
