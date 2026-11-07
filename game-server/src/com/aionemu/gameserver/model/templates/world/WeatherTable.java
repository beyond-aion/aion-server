package com.aionemu.gameserver.model.templates.world;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

/**
 * Weathers of one map. Zone 0 is the whole map, zones 1..zone_count are the weather zones of the map.
 *
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WeatherTable")
public class WeatherTable {

	@XmlElement(name = "weather")
	protected List<WeatherEntry> entries;

	@XmlAttribute(name = "zone_count", required = true)
	protected int zoneCount;

	@XmlAttribute(name = "id", required = true)
	protected int mapId;

	@XmlTransient
	private Map<String, WeatherEntry> entriesByName;

	@XmlTransient
	private Map<Integer, WeatherEntry> entriesByCode;

	@XmlTransient
	private Map<Integer, List<WeatherEntry>> entriesByZone;

	void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		if (entries == null)
			entries = new ArrayList<>();
		entriesByName = new HashMap<>();
		entriesByCode = new HashMap<>();
		entriesByZone = new HashMap<>();
		for (int index = 0; index < entries.size(); index++) {
			WeatherEntry entry = entries.get(index);
			if (entry.getCode() != index + 1)
				throw new IllegalArgumentException("Weather " + entry.getName() + " of map " + mapId + " has code " + entry.getCode() + " instead of " + (index + 1));
			if (entry.getZoneId() < 0 || entry.getZoneId() > zoneCount)
				throw new IllegalArgumentException("Weather " + entry.getName() + " of map " + mapId + " has zone " + entry.getZoneId() + " but the map has " + zoneCount);
			if (entry.getOccurProbability() < 0 || entry.getOccurProbability() > 100)
				throw new IllegalArgumentException("Weather " + entry.getName() + " of map " + mapId + " has occur_prob " + entry.getOccurProbability());
			if (entriesByName.put(entry.getName(), entry) != null)
				throw new IllegalArgumentException("Duplicated weather name " + entry.getName() + " in map " + mapId);
		}
		for (WeatherEntry entry : entries) {
			entry.resolveReferences(this);
			entriesByCode.put(entry.getCode(), entry);
			entriesByZone.computeIfAbsent(entry.getZoneId(), _ -> new ArrayList<>()).add(entry);
		}
		// Weathers are rolled in priority order; equal priorities keep the order of the world's file.
		for (List<WeatherEntry> zoneEntries : entriesByZone.values())
			zoneEntries.sort(Comparator.comparingInt(WeatherEntry::getPriority));
	}

	public int getMapId() {
		return mapId;
	}

	public int getZoneCount() {
		return zoneCount;
	}

	public WeatherEntry getEntry(String name) {
		return entriesByName == null ? null : entriesByName.get(name);
	}

	/**
	 * @return The weather with the given code, null if there is no such code in this world. 0 is clear sky.
	 */
	public WeatherEntry getEntry(int code) {
		return entriesByCode == null ? null : entriesByCode.get(code);
	}

	public List<WeatherEntry> getEntries() {
		return entries == null ? List.of() : entries;
	}

	/**
	 * @return Weathers which can be active in the given zone (0 = the whole map), in the order they are rolled in.
	 */
	public List<WeatherEntry> getEntriesForZone(int zoneId) {
		if (entriesByZone == null)
			return List.of();
		return entriesByZone.getOrDefault(zoneId, List.of());
	}
}
