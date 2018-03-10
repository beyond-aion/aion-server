package com.aionemu.commons.utils.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.ExitCode;

/**
 * @author -Nemesiss-
 */
public class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {

	private static final Logger log = LoggerFactory.getLogger(UncaughtExceptionHandler.class);

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		log.error("Critical Error - Thread [" + t.getName() + "] terminated abnormally:", e);
		if (e instanceof OutOfMemoryError) {
			log.error("Trying to exit gracefully with ExitCode.RESTART..."); // we shouldn't even try to regain memory at this point
			System.exit(ExitCode.CODE_RESTART);
		}
	}
}
