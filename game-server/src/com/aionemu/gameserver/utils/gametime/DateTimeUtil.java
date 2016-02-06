package com.aionemu.gameserver.utils.gametime;

import java.util.GregorianCalendar;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.aionemu.gameserver.configs.main.GSConfig;

/**
 * @author Rolandas
 * @modified Neon
 */
public final class DateTimeUtil {

	public static void init() {
		try {
			DateTimeZone.forID(GSConfig.TIME_ZONE_ID);
		} catch (IllegalArgumentException e) {
			throw new Error("Invalid or not supported timezone specified!\nAdd a valid value for GSConfig.TIME_ZONE_ID", e);
		}
	}

	@Deprecated
	public static DateTime getDateTime(GregorianCalendar calendar) {
		// TODO rework all time based stuff to java 8 standards
		return new DateTime(calendar).withZoneRetainFields(DateTimeZone.forID(GSConfig.TIME_ZONE_ID));
	}
}
