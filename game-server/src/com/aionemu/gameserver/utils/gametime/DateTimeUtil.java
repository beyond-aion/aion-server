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

	@Deprecated
	public static DateTime getDateTime(GregorianCalendar calendar) {
		// TODO rework all time based stuff to java 8 standards
		return new DateTime(calendar).withZoneRetainFields(DateTimeZone.forTimeZone(GSConfig.TIME_ZONE));
	}
}
