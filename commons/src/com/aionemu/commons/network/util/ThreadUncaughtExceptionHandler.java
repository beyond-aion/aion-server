package com.aionemu.commons.network.util;

import org.slf4j.LoggerFactory;

import org.slf4j.Logger;
/**
 * @author -Nemesiss-
 */
public class ThreadUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
{
	/**
	 * Logger for this class.
	 */
	private static final Logger	log	= LoggerFactory.getLogger(ThreadUncaughtExceptionHandler.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void uncaughtException(final Thread t, final Throwable e)
	{
		log.error("Critical Error - Thread: " + t.getName() + " terminated abnormaly: " + e, e);
		if (e instanceof OutOfMemoryError)
		{
			// TODO try get some memory or restart
		}
		// TODO! some threads should be "restarted" on error
	}
}
