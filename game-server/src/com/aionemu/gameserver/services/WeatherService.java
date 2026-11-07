package com.aionemu.gameserver.services;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.materials.MaterialActCondition;
import com.aionemu.gameserver.model.templates.world.WeatherEntry;
import com.aionemu.gameserver.model.templates.world.WeatherState;
import com.aionemu.gameserver.model.templates.world.WeatherTable;
import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;
import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.model.templates.zone.ZoneInfo;
import com.aionemu.gameserver.services.weather.WeatherMap;
import com.aionemu.gameserver.services.weather.WeatherZone;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.time.gametime.GameTime;
import com.aionemu.gameserver.world.MapRegion;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMap;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * Weather of every world instance, driven by the game clock. The schedule parameters live in {@code weather_table.xml}:
 * a zone rolls once per game hour when no weather is running and then follows its own phases. Instances and houses are
 * skipped, as retail sends them no S_WEATHER; the client shows the default weather of their level inside the zone.
 *
 * @author ATracer, Kwazar, Rolandas
 */
public class WeatherService {

	private static final Logger log = LoggerFactory.getLogger(WeatherService.class);

	private static final int GAME_MINUTES_PER_HOUR = (int) Duration.ofHours(1).toMinutes();
	// The schedule counts a month as 31 game days, whatever the real calendar says.
	private static final int GAME_MINUTES_PER_MONTH = (int) Duration.ofDays(31).toMinutes();

	private final Map<WorldMapInstance, WeatherMap> weatherMaps = new ConcurrentHashMap<>();
	private final Random random = new Random();

	private int lastMinute = -1;

	public static final WeatherService getInstance() {
		return SingletonHolder.instance;
	}

	private WeatherService() {
		for (WorldMapTemplate template : DataManager.WORLD_MAPS_DATA) {
			int mapId = template.getMapId();
			WeatherTable table = DataManager.MAP_WEATHER_DATA.getWeather(mapId);
			WorldMap worldMap = World.getInstance().getWorldMap(mapId);
			if (table == null || worldMap == null || worldMap.isInstanceType())
				continue;
			validateZoneCount(mapId, table);
			for (WorldMapInstance instance : worldMap)
				weatherMaps.put(instance, new WeatherMap(table));
		}
		log.info("Loaded weather for {} world instances", weatherMaps.size());
	}

	/**
	 * The zone count of the table has to match the weather zones of the zone data, otherwise a code would be shown
	 * in the wrong area or not at all.
	 */
	private void validateZoneCount(int mapId, WeatherTable table) {
		int zones = 0;
		for (ZoneInfo zone : DataManager.ZONE_DATA.getZones(mapId))
			if (zone.getZoneTemplate().getZoneType() == ZoneClassName.WEATHER)
				zones++;
		if (zones != table.getZoneCount())
			log.warn("Weather of map {} declares {} zones, the zone data has {}", mapId, table.getZoneCount(), zones);
	}

	/**
	 * Called for every game minute. A jump of the game clock (for example the admin {@code //time}) applies every phase
	 * change which became due in between, so a jump does not lose weather phases.
	 */
	public synchronized void onGameMinutes(GameTime gameTime) {
		int now = gameTime.getTime();
		if (lastMinute < 0) {
			// Start from the beginning of the current hour, so a world does not sit weatherless until the next one.
			processMinutes(now - now % GAME_MINUTES_PER_HOUR, now, false);
		} else if (now > lastMinute) {
			processMinutes(lastMinute + 1, now, true);
		} else if (now < lastMinute) {
			// The clock was moved back: what is running keeps the time it had left instead of waiting for the clock to
			// catch up with its old deadline, which would also block the hourly rolls until then.
			int shift = now - lastMinute;
			weatherMaps.values().forEach(weatherMap -> weatherMap.shift(shift));
		}
		lastMinute = now;
	}

	private void processMinutes(int fromMinute, int toMinute, boolean broadcast) {
		Set<WorldMapInstance> changed = new HashSet<>();
		for (int minute = fromMinute; minute <= toMinute; minute++) {
			int month = (minute / GAME_MINUTES_PER_MONTH) % 12;
			int hour = (minute / GAME_MINUTES_PER_HOUR) % 24;
			boolean hourBoundary = minute % GAME_MINUTES_PER_HOUR == 0;
			for (Map.Entry<WorldMapInstance, WeatherMap> entry : weatherMaps.entrySet()) {
				WeatherMap weatherMap = entry.getValue();
				boolean mapChanged = weatherMap.advance(minute, random);
				if (hourBoundary)
					mapChanged |= weatherMap.roll(minute, month, hour, random);
				if (mapChanged)
					changed.add(entry.getKey());
			}
		}
		if (broadcast)
			changed.forEach(this::sendWeather);
	}

	public void loadWeather(Player player) {
		WeatherMap weatherMap = weatherMaps.get(player.getWorldMapInstance());
		if (weatherMap != null)
			PacketSendUtility.sendPacket(player, weatherMap.buildPacket());
	}

	/**
	 * @param weatherCode 0 clears the weather, otherwise the code of a weather of this map.
	 * @return False if the map has no weather table or no such code.
	 */
	public synchronized boolean changeWeather(Player player, int weatherCode) {
		WeatherTable table = DataManager.MAP_WEATHER_DATA.getWeather(player.getWorldId());
		if (table == null)
			return false;
		WeatherEntry entry = null;
		if (weatherCode != 0 && (entry = table.getEntry(weatherCode)) == null)
			return false;
		boolean changed = false;
		for (WorldMapInstance instance : getWeatherInstances(player.getWorldId())) {
			weatherMaps.get(instance).setWeather(entry);
			sendWeather(instance);
			changed = true;
		}
		return changed;
	}

	/**
	 * Ends the weather which is running on the map and rolls a new one right away, without waiting for an hour
	 * boundary. Used by the admin command; the natural cycle needs no such transition.
	 */
	public synchronized boolean nextWeather(Player player) {
		int now = GameTimeService.getInstance().getGameTime().getTime();
		int month = (now / GAME_MINUTES_PER_MONTH) % 12;
		int hour = (now / GAME_MINUTES_PER_HOUR) % 24;
		boolean changed = false;
		for (WorldMapInstance instance : getWeatherInstances(player.getWorldId())) {
			WeatherMap weatherMap = weatherMaps.get(instance);
			weatherMap.next(now, month, hour, random);
			sendWeather(instance);
			changed = true;
		}
		return changed;
	}

	private Set<WorldMapInstance> getWeatherInstances(int mapId) {
		WorldMap worldMap = World.getInstance().getWorldMap(mapId);
		Set<WorldMapInstance> instances = new HashSet<>();
		if (worldMap != null)
			for (WorldMapInstance instance : worldMap)
				if (weatherMaps.containsKey(instance))
					instances.add(instance);
		return instances;
	}

	private void sendWeather(WorldMapInstance instance) {
		WeatherMap weatherMap = weatherMaps.get(instance);
		if (weatherMap == null || instance.getPlayersInside().isEmpty())
			return;
		PacketSendUtility.broadcastToWorld(weatherMap.buildPacket(), player -> player.isSpawned() && player.getWorldMapInstance() == instance);
	}

	/**
	 * Weather the client shows at the position: the weather of the zone it is in, or the weather of the whole map when
	 * the position is outside of every weather zone or nothing runs in its zone.
	 *
	 * @return The weather or null for clear sky.
	 */
	public WeatherEntry getWeatherAt(Creature creature) {
		return getWeatherAt(creature.getWorldMapInstance(), creature.getX(), creature.getY(), creature.getZ());
	}

	/**
	 * @see #getWeatherAt(Creature)
	 */
	public WeatherEntry getWeatherAt(WorldMapInstance instance, float x, float y, float z) {
		WeatherMap weatherMap = weatherMaps.get(instance);
		if (weatherMap == null)
			return null;
		WeatherEntry zoneWeather = getZoneWeatherAt(weatherMap, instance, x, y, z);
		if (zoneWeather != null)
			return zoneWeather;
		WeatherZone world = weatherMap.getZone(0);
		return world == null ? null : world.getCurrent();
	}

	/**
	 * The weather of the weather zone the position is in. A weather without state (a lead-in, fog, clouds) is still the
	 * weather the client shows there, so it is not replaced by the weather of the map.
	 */
	private WeatherEntry getZoneWeatherAt(WeatherMap weatherMap, WorldMapInstance instance, float x, float y, float z) {
		ZoneInstance zone = findWeatherZone(instance, x, y, z);
		if (zone == null)
			return null;
		WeatherZone weatherZone = weatherMap.getZone(DataManager.ZONE_DATA.getWeatherZoneId(zone.getZoneTemplate()));
		return weatherZone == null ? null : weatherZone.getCurrent();
	}

	private static ZoneInstance findWeatherZone(WorldMapInstance instance, float x, float y, float z) {
		MapRegion region = instance.getRegion(x, y, z);
		return region == null ? null : region.findZone(ZoneClassName.WEATHER, x, y, z);
	}

	/**
	 * @return Weather zone the creature stands in, 0 when it is outside of every zone of the map.
	 */
	public int getWeatherZoneIdAt(Creature creature) {
		return getWeatherZoneIdAt(creature.getWorldMapInstance(), creature.getX(), creature.getY(), creature.getZ());
	}

	/**
	 * @see #getWeatherZoneIdAt(Creature)
	 */
	public int getWeatherZoneIdAt(WorldMapInstance instance, float x, float y, float z) {
		ZoneInstance zone = findWeatherZone(instance, x, y, z);
		return zone == null ? 0 : DataManager.ZONE_DATA.getWeatherZoneId(zone.getZoneTemplate());
	}

	/**
	 * @return Game minutes until the current weather changes, -1 when nothing is running or the position has no weather.
	 */
	public int getMinutesUntilNextChange(Creature creature) {
		WeatherMap weatherMap = weatherMaps.get(creature.getWorldMapInstance());
		if (weatherMap == null)
			return -1;
		WeatherZone zone = weatherMap.getZone(getWeatherZoneIdAt(creature));
		if (zone == null || zone.getNextEventMinute() < 0)
			return -1;
		return Math.max(0, zone.getNextEventMinute() - GameTimeService.getInstance().getGameTime().getTime());
	}

	/**
	 * @return True if all conditions are met at the creature's position. Night lasts from 20:00 to 06:59, sunny means neither rain nor snow.
	 */
	public boolean matchesMaterialActConditions(Creature creature, List<MaterialActCondition> conditions) {
		for (MaterialActCondition condition : conditions) {
			if (condition == MaterialActCondition.NIGHT) {
				int hour = GameTimeService.getInstance().getGameTime().getHour();
				if (hour >= 7 && hour < 20)
					return false;
			} else if (condition == MaterialActCondition.SUNNY) {
				if (isRainingAt(creature))
					return false;
			}
		}
		return true;
	}

	/**
	 * A weather without state (a lead-in, fog, clouds) does not hide the rain or snow of the whole map.
	 */
	private boolean isRainingAt(Creature creature) {
		WorldMapInstance instance = creature.getWorldMapInstance();
		WeatherMap weatherMap = weatherMaps.get(instance);
		if (weatherMap == null)
			return false;
		WeatherEntry weather = getZoneWeatherAt(weatherMap, instance, creature.getX(), creature.getY(), creature.getZ());
		if (weather == null || weather.getState() == null) {
			WeatherZone world = weatherMap.getZone(0);
			weather = world == null ? null : world.getCurrent();
		}
		return weather != null && (weather.getState() == WeatherState.RAIN || weather.getState() == WeatherState.SNOW);
	}

	private static class SingletonHolder {

		protected static final WeatherService instance = new WeatherService();
	}
}
