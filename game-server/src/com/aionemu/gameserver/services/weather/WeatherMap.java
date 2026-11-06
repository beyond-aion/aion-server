package com.aionemu.gameserver.services.weather;

import java.util.Random;

import com.aionemu.gameserver.model.templates.world.WeatherEntry;
import com.aionemu.gameserver.model.templates.world.WeatherTable;
import com.aionemu.gameserver.network.aion.serverpackets.SM_WEATHER;

/**
 * Weather of one world instance
 */
public class WeatherMap {

	/**
	 * Index 0 is the weather of the whole map, indexes 1..zone_count are the map's weather zones.
	 */
	private final WeatherZone[] zones;

	public WeatherMap(WeatherTable table) {
		zones = new WeatherZone[table.getZoneCount() + 1];
		for (int zoneId = 0; zoneId < zones.length; zoneId++)
			zones[zoneId] = new WeatherZone(table.getEntriesForZone(zoneId));
	}

	/**
	 * @return True if any zone changed its visible weather.
	 */
	public boolean advance(int now, Random random) {
		boolean changed = false;
		for (WeatherZone zone : zones)
			changed |= zone.advance(now, random);
		return changed;
	}

	/**
	 * @return True if any zone changed its visible weather.
	 */
	public boolean roll(int now, int month, int hour, Random random) {
		boolean changed = false;
		for (WeatherZone zone : zones)
			changed |= zone.roll(now, month, hour, random);
		return changed;
	}

	/**
	 * Moves the pending phase changes of every zone by {@code minutes}, see {@link WeatherZone#shift(int)}.
	 */
	public void shift(int minutes) {
		for (WeatherZone zone : zones)
			zone.shift(minutes);
	}

	/**
	 * @return True if any zone changed its visible weather.
	 */
	public boolean next(int now, int month, int hour, Random random) {
		boolean changed = false;
		for (WeatherZone zone : zones)
			changed |= zone.next(now, month, hour, random);
		return changed;
	}

	/**
	 * Puts the weather on the zone it belongs to, or clears the whole map when {@code entry} is null.
	 */
	public void setWeather(WeatherEntry entry) {
		for (int zoneId = 0; zoneId < zones.length; zoneId++)
			if (entry == null || entry.getZoneId() == zoneId)
				zones[zoneId].setWeather(entry);
	}

	/**
	 * @return The zone weather, null if the zone does not exist or has nothing running.
	 */
	public WeatherZone getZone(int zoneId) {
		return zoneId < 0 || zoneId >= zones.length ? null : zones[zoneId];
	}

	public int getZoneCount() {
		return zones.length - 1;
	}

	public SM_WEATHER buildPacket() {
		int[] zoneCodes = new int[zones.length - 1];
		for (int zoneId = 1; zoneId < zones.length; zoneId++)
			zoneCodes[zoneId - 1] = zones[zoneId].getCode();
		return new SM_WEATHER(zones[0].getCode(), zoneCodes);
	}
}
