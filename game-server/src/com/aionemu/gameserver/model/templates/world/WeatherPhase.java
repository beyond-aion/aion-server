package com.aionemu.gameserver.model.templates.world;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

/**
 * A weather a weather may lead into: its sign (the lead-in), its remain (the tail) or one of its alternatives. The
 * name is resolved to the weather itself when the table is loaded.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WeatherPhase")
public class WeatherPhase {

	@XmlAttribute(name = "name", required = true)
	private String name;

	@XmlAttribute(name = "probability")
	private int probability;

	@XmlAttribute(name = "duration")
	private int duration;

	@XmlAttribute(name = "duration_ex")
	private int durationEx;

	@XmlTransient
	private WeatherEntry weather;

	public String getName() {
		return name;
	}

	/**
	 * @return Chance in percent that this phase is played; 0 means it is never played.
	 */
	public int getProbability() {
		return probability;
	}

	/**
	 * @return Duration of the phase in game minutes, the random part of it is added by {@link #getDurationEx()}.
	 */
	public int getDuration() {
		return duration;
	}

	public int getDurationEx() {
		return durationEx;
	}

	/**
	 * @return The weather of the phase, resolved when the table is loaded.
	 */
	public WeatherEntry getWeather() {
		return weather;
	}

	void resolve(WeatherTable table, WeatherEntry owner, String role) {
		weather = table.getEntry(name);
		if (weather == null)
			throw new IllegalArgumentException("Weather " + owner.getName() + " refers to unknown " + role + " weather " + name);
		if (weather.getZoneId() != owner.getZoneId())
			throw new IllegalArgumentException("Weather " + owner.getName() + " and its " + role + " weather " + name + " belong to different zones");
	}
}
