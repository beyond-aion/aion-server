package com.aionemu.gameserver.model.siege;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.L10n;

/**
 * @author Sarynth
 */
public enum SiegeRace implements L10n {

	ELYOS(Race.ELYOS),
	ASMODIANS(Race.ASMODIANS),
	BALAUR(2, 900242);

	private final int raceId;
	private final int l10nId;

	SiegeRace(Race race) {
		this(race.getRaceId(), race.getL10nId());
	}

	SiegeRace(int id, int l10nId) {
		this.raceId = id;
		this.l10nId = l10nId;
	}

	public int getRaceId() {
		return raceId;
	}

	public static SiegeRace getByRace(Race race) {
		return switch (race) {
			case ASMODIANS -> SiegeRace.ASMODIANS;
			case ELYOS -> SiegeRace.ELYOS;
			default -> SiegeRace.BALAUR;
		};
	}

	@Override
	public int getL10nId() {
		return l10nId;
	}
}
