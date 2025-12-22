package com.aionemu.gameserver.controllers.effect;

import com.aionemu.gameserver.model.stats.container.StatEnum;

public enum CumulativeResistType {

	FEAR,
	PARALYZE,
	SLEEP;

	public static CumulativeResistType get(StatEnum stat) {
		return switch (stat) {
			case FEAR_RESISTANCE -> FEAR;
			case PARALYZE_RESISTANCE -> PARALYZE;
			case SLEEP_RESISTANCE -> SLEEP;
			default -> null;
		};
	}
}
