package com.aionemu.gameserver.model.siege;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.utils.ChatUtil;

/**
 * @author Sarynth
 */
public enum SiegeRace {
	ELYOS(Race.ELYOS),
	ASMODIANS(Race.ASMODIANS),
	BALAUR(2, ChatUtil.l10n(900242));

	private final int raceId;
	private final String l10n;

	private SiegeRace(Race race) {
		this(race.getRaceId(), race.getL10n());
	}

	private SiegeRace(int id, String l10n) {
		this.raceId = id;
		this.l10n = l10n;
	}

	public int getRaceId() {
		return this.raceId;
	}

	public static SiegeRace getByRace(Race race) {
		switch (race) {
			case ASMODIANS:
				return SiegeRace.ASMODIANS;
			case ELYOS:
				return SiegeRace.ELYOS;
			default:
				return SiegeRace.BALAUR;
		}
	}

	public String getL10n() {
		return l10n;
	}
}
