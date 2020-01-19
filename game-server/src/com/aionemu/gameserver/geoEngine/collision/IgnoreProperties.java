package com.aionemu.gameserver.geoEngine.collision;

import com.aionemu.gameserver.model.Race;

public class IgnoreProperties {
	private Race race;
	private int staticId;

	public static IgnoreProperties of(Race race, int staticId) {
		IgnoreProperties properties = new IgnoreProperties();
		properties.race = race;
		properties.staticId = staticId;
		return properties;
	}

	public static IgnoreProperties of(Race race) {
		return of(race, -1);
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
