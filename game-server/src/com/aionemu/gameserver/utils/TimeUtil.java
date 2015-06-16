package com.aionemu.gameserver.utils;

/**
 * @author ATracer
 */
public class TimeUtil {

	/**
	 * Check whether supplied time in ms is expired
	 */
	public static final boolean isExpired(long time) {
		return time < System.currentTimeMillis();
	}
}
