package com.aionemu.gameserver.model.templates.world;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WeatherTable", propOrder = { "zoneData" })
public class WeatherTable {

	@XmlElement(name = "table", required = true)
	protected List<WeatherEntry> zoneData;

	@XmlAttribute(name = "weather_count", required = true)
	protected int weatherCount;

	@XmlAttribute(name = "zone_count", required = true)
	protected int zoneCount;

	@XmlAttribute(name = "id", required = true)
	protected int mapId;

	public List<WeatherEntry> getZoneData() {
		return zoneData;
	}

	public int getMapId() {
		return mapId;
	}

	public int getZoneCount() {
		return zoneCount;
	}

	public int getWeatherCount() {
		return weatherCount;
	}

	public WeatherEntry getWeatherAfter(WeatherEntry entry) {
		if (entry == null || entry.getWeatherName() == null || entry.isAfter())
			return null;
		for (WeatherEntry we : getZoneData()) {
			if (we.getZoneId() != entry.getZoneId())
				continue;
			if (entry.getWeatherName().equals(we.getWeatherName())) {
				if (entry.isBefore() && !we.isBefore() && !we.isAfter())
					return we;
				else if (!entry.isBefore() && !entry.isAfter() && we.isAfter())
					return we;
			}
		}
		return null;
	}

	public List<WeatherEntry> getWeathersForZone(int zoneId) {
		List<WeatherEntry> result = new ArrayList<>();
		for (WeatherEntry entry : getZoneData()) {
			if (entry.getZoneId() == zoneId)
				result.add(entry);
		}
		return result;
	}

}
