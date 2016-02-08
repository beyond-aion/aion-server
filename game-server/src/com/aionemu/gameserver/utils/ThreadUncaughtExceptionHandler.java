package com.aionemu.gameserver.utils;

import java.lang.Thread.UncaughtExceptionHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author -Nemesiss-
 */
public class ThreadUncaughtExceptionHandler implements UncaughtExceptionHandler {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(ThreadUncaughtExceptionHandler.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error("Critical Error - Thread: " + t.getName() + " terminated abnormaly", e);
		if (e instanceof OutOfMemoryError) {
			// TODO try get some memory or restart
			log.error("Out of memory! You should get more memory!");
		}
		// TODO! some threads should be "restarted" on error
	}
}
