package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.world.WeatherTable;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "weatherData" })
@XmlRootElement(name = "weather")
public class MapWeatherData {

	@XmlElement(name = "map", required = true)
	private List<WeatherTable> weatherData;

	@XmlTransient
	private final Map<Integer, WeatherTable> mapWeather = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (WeatherTable table : weatherData) {
			mapWeather.put(table.getMapId(), table);
		}
		weatherData = null;
	}

	public WeatherTable getWeather(int mapId) {
		return mapWeather.get(mapId);
	}

	public int size() {
		return mapWeather.size();
	}

}
