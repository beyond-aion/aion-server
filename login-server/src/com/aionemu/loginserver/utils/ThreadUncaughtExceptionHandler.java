package com.aionemu.loginserver.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author -Nemesiss-
 */
public class ThreadUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(ThreadUncaughtExceptionHandler.class);

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error("Critical Error - Thread: " + t.getName() + " terminated abnormaly: " + e, e);
		if (e instanceof OutOfMemoryError) {
			// TODO try get some memory or restart
		}
		// TODO! some threads should be "restarted" on error
	}
}
