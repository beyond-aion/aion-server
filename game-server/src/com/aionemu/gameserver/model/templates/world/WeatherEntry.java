package com.aionemu.gameserver.model.templates.world;

import java.util.List;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

/**
 * One weather of a map: when it may start, how long it and its phases last, and which weathers may precede and follow it.
 *
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WeatherEntry")
public class WeatherEntry {

	private static final int ALL_MONTHS = (1 << 12) - 1;

	@XmlAttribute(name = "code", required = true)
	private int code;

	@XmlAttribute(name = "name", required = true)
	private String name;

	@XmlAttribute(name = "zone", required = true)
	private int zoneId;

	@XmlAttribute(name = "priority", required = true)
	private int priority;

	@XmlAttribute(name = "months")
	private String months;

	@XmlAttribute(name = "hours")
	private String hours;

	@XmlAttribute(name = "occur_prob", required = true)
	private int occurProbability;

	@XmlAttribute(name = "occur_delay")
	private int occurDelay;

	@XmlAttribute(name = "duration", required = true)
	private int duration;

	@XmlAttribute(name = "duration_ex")
	private int durationEx;

	@XmlElement(name = "sign")
	private WeatherPhase sign;

	@XmlElement(name = "remain")
	private WeatherPhase remain;

	@XmlElement(name = "sub")
	private List<WeatherPhase> subWeathers;

	@XmlAttribute(name = "state")
	private WeatherState state;

	@XmlTransient
	private int monthMask;

	@XmlTransient
	private int hourMask;

	/**
	 * A weather without {@code months} may start all year round, an empty {@code months} never; a weather without
	 * {@code hours} never starts on its own.
	 */
	void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		monthMask = months == null ? ALL_MONTHS : parseMask(months, -1);
		hourMask = parseMask(hours, 0);
	}

	/**
	 * Parses {@code "1,3-5,12"}. {@code shift} maps a value to its bit: months are stored from 1, hours from 0.
	 */
	static int parseMask(String spec, int shift) {
		if (spec == null || spec.isBlank())
			return 0;
		int mask = 0;
		for (String part : spec.split(",")) {
			part = part.trim();
			if (part.isEmpty())
				continue;
			int dash = part.indexOf('-');
			if (dash < 0) {
				mask |= 1 << (Integer.parseInt(part) + shift);
			} else {
				int from = Integer.parseInt(part.substring(0, dash).trim());
				int to = Integer.parseInt(part.substring(dash + 1).trim());
				for (int value = from; value <= to; value++)
					mask |= 1 << (value + shift);
			}
		}
		return mask;
	}

	/**
	 * @param month Month of the game calendar, 0-11.
	 * @param hour  Hour of the game day, 0-23.
	 */
	public boolean isDueIn(int month, int hour) {
		return (hourMask & (1 << hour)) != 0 && (monthMask & (1 << month)) != 0;
	}

	public int getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public int getZoneId() {
		return zoneId;
	}

	public int getPriority() {
		return priority;
	}

	public int getOccurProbability() {
		return occurProbability;
	}

	public int getOccurDelay() {
		return occurDelay;
	}

	public int getDuration() {
		return duration;
	}

	public int getDurationEx() {
		return durationEx;
	}

	/**
	 * @return The lead-in phase, null if the weather starts right away.
	 */
	public WeatherPhase getSign() {
		return sign;
	}

	/**
	 * @return The tail phase, null if the weather ends right away.
	 */
	public WeatherPhase getRemain() {
		return remain;
	}

	/**
	 * @return Alternative weathers which may follow the main phase, in the order they are rolled for, never null.
	 */
	public List<WeatherPhase> getSubWeathers() {
		return subWeathers == null ? List.of() : subWeathers;
	}

	/**
	 * @return The weather state or null if the weather has none (clear sky, clouds, fog, the lead-in of a rain etc.)
	 */
	public WeatherState getState() {
		return state;
	}

	/**
	 * Resolves the phases against the weathers of the same zone. A world whose weather refers to a weather of another
	 * zone or to an unknown name does not load.
	 */
	void resolveReferences(WeatherTable table) {
		if (sign != null)
			sign.resolve(table, this, "sign");
		if (remain != null)
			remain.resolve(table, this, "remain");
		if (subWeathers != null)
			for (WeatherPhase subWeather : subWeathers)
				subWeather.resolve(table, this, "sub");
	}
}
