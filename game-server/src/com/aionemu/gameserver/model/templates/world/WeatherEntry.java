package com.aionemu.gameserver.model.templates.world;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WeatherEntry")
public class WeatherEntry {

	public static final WeatherEntry NONE = new WeatherEntry();

	@XmlAttribute(name = "zone_id", required = true)
	private int zoneId;

	@XmlAttribute(name = "code", required = true)
	private int weatherCode;

	@XmlAttribute(name = "rank", required = true)
	private int rank;

	@XmlAttribute(name = "name")
	private String weatherName;

	@XmlAttribute(name = "before")
	private boolean isBefore;

	@XmlAttribute(name = "after")
	private boolean isAfter;
	@XmlAttribute(name = "state")
	private WeatherState state;

	private WeatherEntry() {
	}

	public WeatherEntry(int zoneId, int weatherCode) {
		this.zoneId = zoneId;
		this.weatherCode = weatherCode;
	}

	public int getZoneId() {
		return zoneId;
	}

	public int getCode() {
		return weatherCode;
	}

	public int getRank() {
		return rank;
	}

	public boolean isBefore() {
		return isBefore;
	}

	public boolean isAfter() {
		return isAfter;
	}

	public String getWeatherName() {
		return weatherName;
	}

	/**
	 * @return The weather state or null if the weather has none (clear sky, clouds, fog, the lead-in of a rain etc.)
	 */
	public WeatherState getState() {
		return state;
	}

}
