package com.aionemu.gameserver.model.templates.world;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

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

}
