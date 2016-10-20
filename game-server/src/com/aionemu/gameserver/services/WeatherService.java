package com.aionemu.gameserver.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.world.WeatherEntry;
import com.aionemu.gameserver.model.templates.world.WeatherTable;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_WEATHER;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.gametime.DayTime;
import com.aionemu.gameserver.utils.gametime.GameTime;

import javolution.util.FastTable;

/**
 * @author ATracer, Kwazar
 * @reworked Rolandas
 */
public class WeatherService {

	private Map<WeatherKey, WeatherEntry[]> worldZoneWeathers;

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
				WeatherKey key = new WeatherKey(gameTime, mapId);
				worldZoneWeathers.put(key, new WeatherEntry[table.getZoneCount()]);
				setNextWeather(key);
			}
		}
	}

	/**
	 * Key class used to store date of key creation (for rolling weather usage)
	 * 
	 * @author Kwazar, Rolandas
	 */
	private class WeatherKey {

		private GameTime created;
		private final int mapId;

		public WeatherKey(GameTime createdTime, int mapId) {
			this.created = createdTime;
			this.mapId = mapId;
		}

		public int getMapId() {
			return mapId;
		}

		public GameTime getCreatedTime() {
			return created;
		}

		@Override
		public boolean equals(Object o) {
			WeatherKey other = (WeatherKey) o;
			return this.mapId == other.mapId;
		}

		@Override
		public int hashCode() {
			return mapId;
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
				for (WeatherKey key : worldZoneWeathers.keySet()) {
					key.created = weatherTime;
					setNextWeather(key);
					onWeatherChange(key.getMapId(), null);
				}
			}
		}, Rnd.get(20000, 240000)); // change weather 20s to 4m after daytime change
	}

	private synchronized void setNextWeather(WeatherKey key) {
		WeatherEntry[] weatherEntries = getWeatherEntries(key.getMapId());
		WeatherTable table = DataManager.MAP_WEATHER_DATA.getWeather(key.getMapId());
		for (int zoneIndex = 0; zoneIndex < weatherEntries.length; zoneIndex++) {
			WeatherEntry oldEntry = weatherEntries[zoneIndex];
			WeatherEntry newEntry;
			if (oldEntry == null)
				newEntry = getRandomWeather(key.getCreatedTime(), table, zoneIndex + 1);
			else {
				newEntry = table.getWeatherAfter(oldEntry);
				if (newEntry == null)
					newEntry = getRandomWeather(key.getCreatedTime(), table, zoneIndex + 1);
			}
			weatherEntries[zoneIndex] = newEntry;
		}
	}

	private WeatherEntry getRandomWeather(GameTime createdTime, WeatherTable table, int zoneId) {
		List<WeatherEntry> weathers = table.getWeathersForZone(zoneId);

		int chance = Rnd.get(0, 700);
		// rank 2 occurs twice often than rank 1
		// rank 1 occurs twice often than rank 0
		int rank = 2;
		if (chance > 600)
			rank = 0;
		else if (chance > 400)
			rank = 1;

		List<WeatherEntry> chosenWeather = new FastTable<>();
		while (rank >= 0) {
			for (WeatherEntry entry : weathers) {
				if (entry.getRank() == -1)
					return entry; // constant weather, maybe completely random ?

				if (entry.getRank() == rank)
					chosenWeather.add(entry);
			}
			if (chosenWeather.size() > 0) {
				rank = -1;
				break;
			}
			rank--;
		}

		WeatherEntry newWeather = null;
		if (chosenWeather.size() == 0) {
			// no weather, code = 0
			newWeather = new WeatherEntry();
		} else {
			// almost all weather types have after and before weathers, so chances
			// to pick up are almost equal
			newWeather = chosenWeather.get(Rnd.get(chosenWeather.size()));
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
			if (createdTime.getDayTime() == DayTime.AFTERNOON)
				dayTimeCorrection *= 2; // sunny days more often :)
			chance = Rnd.get(0, 100);
			if ((newWeather.getRank() == 0 && chance > 33 / dayTimeCorrection) || (newWeather.getRank() == 1 && chance > 50 / dayTimeCorrection)
				|| (newWeather.getRank() == 2 && chance > 66 / dayTimeCorrection))
				newWeather = new WeatherEntry();

			// TODO: check snow to not fall in summers
		}
		return newWeather;
	}

	/**
	 * When a player connects, it loads his weather
	 * 
	 * @param player
	 */
	public void loadWeather(Player player) {
		onWeatherChange(player.getWorldId(), player);
	}

	/**
	 * Return the correct key from the worldWeathers Map by the mapId
	 * 
	 * @param map
	 * @return
	 */
	private WeatherKey getWeatherKeyByMapId(int mapId) {
		for (WeatherKey key : worldZoneWeathers.keySet()) {
			if (key.getMapId() == mapId) {
				return key;
			}
		}
		return null;
	}

	/**
	 * @param mapId
	 * @return the WeatherEntry array of the mapId for this session
	 */
	private WeatherEntry[] getWeatherEntries(int mapId) {
		WeatherKey key = getWeatherKeyByMapId(mapId);
		if (key == null)
			return null;
		return worldZoneWeathers.get(key);
	}

	/**
	 * Currently from command only, must know weather code, or just for tests
	 */
	public synchronized void changeRegionWeather(int mapId, int weatherCode) {
		WeatherKey key = new WeatherKey(null, mapId);
		WeatherEntry[] weatherEntries = worldZoneWeathers.get(key);
		if (weatherEntries == null)
			return; // do nothing
		for (int i = 0; i < weatherEntries.length; i++) {
			WeatherEntry oldEntry = weatherEntries[i];
			if (oldEntry == null)
				weatherEntries[i] = new WeatherEntry(0, weatherCode);
			else
				weatherEntries[i] = new WeatherEntry(oldEntry.getZoneId(), weatherCode);
		}
		onWeatherChange(mapId, null);
	}

	/**
	 * Allows server to reinitialize Weathers for all regions TODO: not thread safe if run by admin
	 */
	public synchronized void resetWeather() {
		Set<WeatherKey> loadedWeathers = new HashSet<>(worldZoneWeathers.keySet());
		for (WeatherKey key : loadedWeathers) {
			WeatherEntry[] oldEntries = worldZoneWeathers.get(key);
			for (int i = 0; i < oldEntries.length; i++) {
				oldEntries[i] = new WeatherEntry(oldEntries[i].getZoneId(), 0);
			}
			onWeatherChange(key.getMapId(), null);
		}
	}

	public int getWeatherCode(int mapId, int weatherZoneId) {
		WeatherEntry[] weatherEntries = getWeatherEntries(mapId);
		for (WeatherEntry entry : weatherEntries) {
			if (entry != null && entry.getZoneId() == weatherZoneId)
				return entry.getCode();
		}
		return 0;
	}

	/**
	 * triggers the update of weather to all players
	 * 
	 * @param world
	 * @param worldMap
	 * @param player
	 *          if null -> weather is broadcasted to all players in world
	 */
	private void onWeatherChange(int mapId, Player player) {
		WeatherEntry[] weatherEntries = getWeatherEntries(mapId);

		if (weatherEntries == null)
			return;

		if (player == null) {
			PacketSendUtility.broadcastToWorld(new SM_WEATHER(weatherEntries), p -> p.isSpawned() && p.getWorldId() == mapId);
		} else {
			PacketSendUtility.sendPacket(player, new SM_WEATHER(weatherEntries));
		}
	}

	@SuppressWarnings("synthetic-access")
	private static class SingletonHolder {

		protected static final WeatherService instance = new WeatherService();
	}

}
