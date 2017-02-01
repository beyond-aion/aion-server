package com.aionemu.commons.utils.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author -Nemesiss-
 */
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(UncaughtExceptionHandler.class);

	@Override
	public void uncaughtException(final Thread t, final Throwable e) {
		log.error("Critical Error - Thread [" + t.getName() + "] terminated abnormally:", e);
		if (e instanceof OutOfMemoryError) {
			// TODO try get some memory or restart
		}
		// TODO! some threads should be "restarted" on error
	}
}
