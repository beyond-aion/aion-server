package com.aionemu.gameserver.geoEngine.collision;

import com.aionemu.gameserver.model.Race;

public class IgnoreProperties {

	public static final IgnoreProperties ELYOS = new IgnoreProperties(Race.ELYOS, 0);
	public static final IgnoreProperties ASMODIANS = new IgnoreProperties(Race.ASMODIANS, 0);
	public static final IgnoreProperties BALAUR = new IgnoreProperties(Race.DRAKAN, 0);
	public static final IgnoreProperties ANY_RACE = new IgnoreProperties(null, 0);
	private final Race race;
	private final int staticId;

	private IgnoreProperties(Race race, int staticId) {
		this.race = race;
		this.staticId = staticId;
	}

	public static IgnoreProperties of(Race race, int staticId) {
		if (staticId == 0) {
			if (race == Race.ELYOS)
				return ELYOS;
			if (race == Race.ASMODIANS)
				return ASMODIANS;
			if (race == Race.DRAKAN)
				return BALAUR;
		}
		return new IgnoreProperties(race, staticId);

	}

	public static IgnoreProperties of(Race race) {
		return of(race, 0);
	}

	public static IgnoreProperties of(int staticId) {
		return of(null, staticId);
	}

	public Race getRace() {
		return race;
	}

	public int getStaticId() {
		return staticId;
	}

	@Override
	public String toString() {
		return "[IgnoreProperties] Race: " + this.race + " staticId: " + this.staticId;
	}
}
