package com.aionemu.gameserver.services.mail;

/**
 * @author Rolandas
 */
public enum AbyssSiegeLevel {
	NONE(0),
	HERO_DECORATION(1),
	MEDAL(2),
	ELITE_SOLDIER(3),
	VETERAN_SOLDIER(4);

	private int value;

	private AbyssSiegeLevel(int value) {
		this.value = value;
	}

	public int getId() {
		return this.value;
	}

	public static AbyssSiegeLevel getLevelById(int id) {
		for (AbyssSiegeLevel al : values()) {
			if (al.getId() == id) {
				return al;
			}
		}
		throw new IllegalArgumentException("There is no AbyssSiegeLevel with ID " + id);
	}
}
