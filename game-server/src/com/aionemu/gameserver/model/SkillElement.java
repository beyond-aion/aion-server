package com.aionemu.gameserver.model;

import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author xavier
 */
public enum SkillElement {
	NONE(null),
	FIRE(StatEnum.FIRE_RESISTANCE),
	WATER(StatEnum.WATER_RESISTANCE),
	WIND(StatEnum.WIND_RESISTANCE),
	EARTH(StatEnum.EARTH_RESISTANCE),
	LIGHT(StatEnum.LIGHT_RESISTANCE),
	DARK(StatEnum.DARK_RESISTANCE),;
	private final StatEnum statEnum;

	SkillElement(StatEnum statEnum) {
		this.statEnum = statEnum;
	}

	public StatEnum getStatForElement() {
		return statEnum;
	}
}
