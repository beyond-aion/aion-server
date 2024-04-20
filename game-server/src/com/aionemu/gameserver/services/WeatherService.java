package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.world.WeatherEntry;
import com.aionemu.gameserver.model.templates.world.WeatherTable;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.network.aion.serverpackets.SM_WEATHER;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.time.gametime.DayTime;
import com.aionemu.gameserver.utils.time.gametime.GameTime;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author ATracer, Kwazar, Rolandas
 */
public class WeatherService {

	private final Map<Integer, WeatherEntry[]> worldZoneWeathers;

	public static final WeatherService getInstance() {
		return SingletonHolder.instance;
	}

	private WeatherService() {
		worldZoneWeathers = new HashMap<>();
		GameTime gameTime = GameTimeService.getInstance().getGameTime().clone();
		for (WorldMapTemplate worldMapTemplate : DataManager.WORLD_MAPS_DATA) {
			int mapId = worldMapTemplate.getMapId();
			WeatherTable table = DataManager.MAP_WEATHER_DATA.getWeather(mapId);
			if (table != null) {
				WeatherEntry[] weatherEntries = new WeatherEntry[table.getZoneCount()];
				worldZoneWeathers.put(mapId, weatherEntries);
				setNextWeather(mapId, weatherEntries, gameTime);
			}
		}
	}

	/**
	 * Triggered on every day time change (4x every two hours, since an in-game day equals 120 minutes)
	 */
	public void checkWeathersTime() {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				GameTime weatherTime = GameTimeService.getInstance().getGameTime().clone();
				for (Entry<Integer, WeatherEntry[]> e : worldZoneWeathers.entrySet()) {
					int mapId = e.getKey();
					WeatherEntry[] weatherEntries = e.getValue();
					setNextWeather(mapId, weatherEntries, weatherTime);
					PacketSendUtility.broadcastToWorld(new SM_WEATHER(weatherEntries), p -> p.isSpawned() && p.getWorldId() == mapId);
				}
			}
		}, Rnd.get(20000, 240000)); // change weather 20s to 4m after daytime change
	}

	private void setNextWeather(int mapId, WeatherEntry[] weatherEntries, GameTime createdTime) {
		WeatherTable table = DataManager.MAP_WEATHER_DATA.getWeather(mapId);
		synchronized (weatherEntries) {
			for (int zoneId = 1; zoneId <= weatherEntries.length; zoneId++) {
				int index = zoneId - 1;
				weatherEntries[index] = nextWeather(weatherEntries[index], table, zoneId, createdTime);
			}
		}
	}

	private WeatherEntry nextWeather(WeatherEntry oldEntry, WeatherTable table, int zoneId, GameTime time) {
		WeatherEntry nextWeather = table.getWeatherAfter(oldEntry);
		return nextWeather == null ? getRandomWeather(time, table, zoneId) : nextWeather;
	}

	private WeatherEntry getRandomWeather(GameTime time, WeatherTable table, int zoneId) {
		List<WeatherEntry> weathers = table.getWeathersForZone(zoneId);

		int rankChance = Rnd.get(0, 6);
		// rank 2 occurs twice often than rank 1
		// rank 1 occurs twice often than rank 0
		int rank;
		if (rankChance == 0)
			rank = 0;
		else if (rankChance <= 2)
			rank = 1;
		else
			rank = 2;

		boolean canSnow = time.getMonth() <= 3 || time.getMonth() >= 11;
		List<WeatherEntry> possibleWeathers = new ArrayList<>();
		while (rank >= 0) {
			for (WeatherEntry entry : weathers) {
				if (entry.getRank() == -1)
					return entry; // constant weather, maybe completely random ?

				if (entry.getRank() == rank && checkSnowCondition(canSnow, entry))
					possibleWeathers.add(entry);
			}
			if (possibleWeathers.size() > 0) {
				rank = -1;
				break;
			}
			rank--;
		}

		WeatherEntry newWeather;
		if (possibleWeathers.isEmpty()) {
			newWeather = WeatherEntry.NONE;
		} else {
			// almost all weather types have after and before weathers, so chances to pick up are almost equal
			newWeather = Rnd.get(possibleWeathers);
			// now find "before" weather if such exists
			if (!newWeather.isBefore()) {
				for (WeatherEntry entry : weathers) {
					if (newWeather.getWeatherName().equals(entry.getWeatherName()) && entry.isBefore()) {
						newWeather = entry;
						break;
					}
				}
			}

			// now to be or not to be -- we don't want weather present every time :P
			// rank 2 is strongest to appear, rank 0 is the weakest
			int dayTimeCorrection = 1;
			if (time.getDayTime() == DayTime.AFTERNOON && !canSnow)
				dayTimeCorrection *= 2; // sunny days more often :)
			float chance = Rnd.chance();
			if ((newWeather.getRank() == 0 && chance >= 33 / dayTimeCorrection) || (newWeather.getRank() == 1 && chance >= 50 / dayTimeCorrection)
				|| (newWeather.getRank() == 2 && chance >= 66 / dayTimeCorrection))
				newWeather = WeatherEntry.NONE;
		}
		return newWeather;
	}

	private boolean checkSnowCondition(boolean canSnow, WeatherEntry entry) {
		if (!canSnow && entry.getWeatherName() != null) {
			switch (entry.getWeatherName()) {
				case "SNOW":
				case "SNOW_BEACH":
					return false; // ALTGARD_SNOW (Altgard) and SNOW_x_WZ0x (Beluslan) are always valid
			}
		}
		return true;
	}

	public void loadWeather(Player player) {
		WeatherEntry[] weatherEntries = worldZoneWeathers.get(player.getWorldId());
		if (weatherEntries != null)
			PacketSendUtility.sendPacket(player, new SM_WEATHER(weatherEntries));
	}

	/**
	 * Changes the weather to the given weather code on the specified map. -1 has a special meaning and will trigger a natural weather change.
	 */
	public boolean changeWeather(int mapId, int weatherCode) {
		WeatherEntry[] weatherEntries = worldZoneWeathers.get(mapId);
		if (weatherEntries == null)
			return false;
		WeatherTable table = DataManager.MAP_WEATHER_DATA.getWeather(mapId);
		GameTime time = GameTimeService.getInstance().getGameTime().clone();
		synchronized (weatherEntries) {
			for (int zoneId = 1; zoneId <= weatherEntries.length; zoneId++) {
				int index = zoneId - 1;
				if (weatherCode == -1)
					weatherEntries[index] = nextWeather(weatherEntries[index], table, zoneId, time);
				else
					weatherEntries[index] = getOrCreateWeatherEntry(zoneId, weatherCode, table);
			}
		}
		PacketSendUtility.broadcastToWorld(new SM_WEATHER(weatherEntries), p -> p.isSpawned() && p.getWorldId() == mapId);
		return true;
	}

	private WeatherEntry getOrCreateWeatherEntry(int zoneId, int weatherCode, WeatherTable table) {
		if (weatherCode == 0) // 0 means sunny aka. no weather
			return WeatherEntry.NONE;
		return table.getZoneData().stream().filter(w -> w.getZoneId() == zoneId && w.getCode() == weatherCode).findFirst()
			.orElseGet(() -> new WeatherEntry(zoneId, weatherCode));
	}

	public WeatherEntry findWeatherEntry(Creature creature) {
		for (ZoneInstance regionZone : creature.findZones()) {
			if (regionZone.getZoneTemplate().getZoneType() == ZoneClassName.WEATHER) {
				int weatherZoneId = DataManager.ZONE_DATA.getWeatherZoneId(regionZone.getZoneTemplate());
				WeatherEntry weatherEntry = getWeatherEntry(creature.getWorldId(), weatherZoneId);
				if (weatherEntry != null)
					return weatherEntry;
			}
		}
		return WeatherEntry.NONE;
	}

	public WeatherEntry getWeatherEntry(int mapId, int weatherZoneId) {
		WeatherEntry[] weatherEntries = worldZoneWeathers.get(mapId);
		if (weatherEntries == null)
			return null;
		return weatherZoneId <= 0 || weatherZoneId > weatherEntries.length ? null : weatherEntries[weatherZoneId - 1];
	}

	private static class SingletonHolder {

		protected static final WeatherService instance = new WeatherService();
	}

}
