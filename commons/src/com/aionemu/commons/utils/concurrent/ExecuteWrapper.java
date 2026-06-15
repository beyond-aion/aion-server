package com.aionemu.commons.utils.concurrent;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.CommonsConfig;

/**
 * @author NB4L1, Neon
 */
public class ExecuteWrapper implements Executor {

	private static final Logger log = LoggerFactory.getLogger(ExecuteWrapper.class);

	private final long expectedMaxExecutionTimeMillis;

	public ExecuteWrapper(long expectedMaxExecutionTimeMillis) {
		this.expectedMaxExecutionTimeMillis = expectedMaxExecutionTimeMillis;
	}

	@Override
	public void execute(Runnable runnable) {
		execute(runnable, expectedMaxExecutionTimeMillis, true);
	}

	public static void execute(Runnable runnable, long expectedMaxExecutionTimeMillis, boolean catchAndLogThrowables) {
		try {
			long begin = System.nanoTime();
			runnable.run();
			long durationNanos = System.nanoTime() - begin;

			if (CommonsConfig.RUNNABLESTATS_ENABLE)
				RunnableStatsManager.handleStats(runnable.getClass(), durationNanos);

			long durationMillis = TimeUnit.NANOSECONDS.toMillis(durationNanos);
			if (durationMillis > expectedMaxExecutionTimeMillis) {
				String name = runnable.getClass().isAnonymousClass() ? runnable.getClass().getName() : runnable.getClass().getSimpleName();
				log.warn(name + " - execution time: " + durationMillis + "ms");
			}
		} catch (Throwable t) {
			if (catchAndLogThrowables)
				log.error("Exception in a Runnable execution:", t);
			else
				throw t;
		}
	}
}
