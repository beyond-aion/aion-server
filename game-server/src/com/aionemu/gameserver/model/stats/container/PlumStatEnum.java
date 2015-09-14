package com.aionemu.gameserver.model.stats.container;

/**
 * @author Cheatkiller
 */
public enum PlumStatEnum {

	PLUM_HP(42, 150),
	PLUM_BOOST_MAGICAL_SKILL(35, 20),
	PLUM_PHISICAL_ATTACK(30, 4),
	PLUM_SPEED(40, 0);

	private int id;
	private int boostValue;

	private PlumStatEnum(int id, int boostValue) {
		this.id = id;
		this.boostValue = boostValue;
	}

	public int getId() {
		return id;
	}

	public int getBoostValue() {
		return boostValue;
	}
}
