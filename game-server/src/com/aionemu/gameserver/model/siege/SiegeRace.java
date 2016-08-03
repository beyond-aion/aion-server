package com.aionemu.gameserver.model.siege;

import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.Race;

/**
 * @author Sarynth
 */
public enum SiegeRace {
	ELYOS(Race.ELYOS),
	ASMODIANS(Race.ASMODIANS),
	BALAUR(2, 1800485);

	private int raceId;
	private DescriptionId descriptionId;

	private SiegeRace(Race race) {
		this.raceId = race.getRaceId();
		this.descriptionId = race.getRaceDescriptionId();
	}

	private SiegeRace(int id, int descriptionId) {
		this.raceId = id;
		this.descriptionId = new DescriptionId(descriptionId);
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

	public static SiegeRace getOppositeRace(SiegeRace race) {
		switch (race) {
			case ASMODIANS:
				return SiegeRace.ELYOS;
			case ELYOS:
				return SiegeRace.ASMODIANS;
			default:
				return null;
		}
	}

	/**
	 * @return the descriptionId
	 */
	public DescriptionId getDescriptionId() {
		return descriptionId;
	}
}
