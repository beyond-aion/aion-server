package com.aionemu.gameserver.utils.time;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;

import com.aionemu.gameserver.configs.main.GSConfig;

/**
 * This class is used to tell the actual server time (not game time!), independent of the systems or JVM's time zone settings. It should be
 * used wherever simple timestamp checks aren't sufficient, to ensure time zone consistency throughout all classes.
 * 
 * @author Neon
 */
public final class ServerTime {

	/**
	 * @return The current server time (not game time!)
	 */
	public static ZonedDateTime now() {
		return ZonedDateTime.now(GSConfig.TIME_ZONE_ID);
	}

	/**
	 * @param localDateTime
	 * @return The server time at the given date
	 */
	public static ZonedDateTime of(LocalDateTime localDateTime) {
		return ZonedDateTime.of(localDateTime, GSConfig.TIME_ZONE_ID);
	}

	/**
	 * @param date
	 * @return The server time at the given (UTC) date
	 */
	public static ZonedDateTime atDate(Date date) {
		return ZonedDateTime.ofInstant(date.toInstant(), GSConfig.TIME_ZONE_ID);
	}

	/**
	 * @param epochMilli
	 *          - the milliseconds since the epoch of 1970-01-01T00:00:00 UTC
	 * @return The server time at the given date
	 */
	public static ZonedDateTime ofEpochMilli(long epochMilli) {
		return ZonedDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), GSConfig.TIME_ZONE_ID);
	}

	/**
	 * @param epochSecond
	 *          - the seconds since the epoch of 1970-01-01T00:00:00 UTC
	 * @return The server time at the given date
	 */
	public static ZonedDateTime ofEpochSecond(long epochSecond) {
		return ZonedDateTime.ofInstant(Instant.ofEpochSecond(epochSecond), GSConfig.TIME_ZONE_ID);
	}

	/**
	 * @param text
	 * @return The server time from the parsed {@link ZonedDateTime}
	 * @see ZonedDateTime#parse(CharSequence)
	 */
	public static ZonedDateTime parse(CharSequence text) {
		return ZonedDateTime.parse(text).withZoneSameInstant(GSConfig.TIME_ZONE_ID);
	}

	/**
	 * @param text
	 * @return The server time from the parsed {@link LocalDateTime}
	 * @see LocalDateTime#parse(CharSequence)
	 */
	public static ZonedDateTime parseLocal(CharSequence text) {
		return of(LocalDateTime.parse(text));
	}

	/**
	 * @return The daylight savings offset in seconds at the current date.
	 */
	public static int getDaylightSavings() {
		return (int) GSConfig.TIME_ZONE_ID.getRules().getDaylightSavings(Instant.now()).getSeconds();
	}

	/**
	 * @return The offset to UTC in seconds at the current date (including daylight savings).
	 */
	public static int getOffset() {
		return GSConfig.TIME_ZONE_ID.getRules().getOffset(Instant.now()).getTotalSeconds();
	}

	/**
	 * @return The standard offset to UTC in seconds (excluding daylight savings).
	 */
	public static int getStandardOffset() {
		return GSConfig.TIME_ZONE_ID.getRules().getStandardOffset(Instant.now()).getTotalSeconds();
	}
}
