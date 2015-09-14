package com.aionemu.gameserver.model;

import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author xavier
 */
public enum SkillElement {
	NONE(0),
	FIRE(1),
	WATER(2),
	WIND(3),
	EARTH(4),
	LIGHT(5),
	DARK(6);

	private int element;

	private SkillElement(int id) {
		this.element = id;
	}

	public int getElementId() {
		return element;
	}

	public static StatEnum getResistanceForElement(SkillElement element) {
		switch (element) {
			case FIRE:
				return StatEnum.FIRE_RESISTANCE;
			case WATER:
				return StatEnum.WATER_RESISTANCE;
			case WIND:
				return StatEnum.WIND_RESISTANCE;
			case EARTH:
				return StatEnum.EARTH_RESISTANCE;
			case LIGHT:
				return StatEnum.ELEMENTAL_RESISTANCE_LIGHT;
			case DARK:
				return StatEnum.ELEMENTAL_RESISTANCE_DARK;
		}
		return null;

	}
}
