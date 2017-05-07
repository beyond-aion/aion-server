package com.aionemu.commons.utils.concurrent;

import java.lang.management.ManagementFactory;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.configs.CommonsConfig;

/**
 * @author NB4L1
 */
public class ExecuteWrapper implements Executor {

	private static final Logger log = LoggerFactory.getLogger(ExecuteWrapper.class);

	@Override
	public void execute(Runnable runnable) {
		execute(runnable, Long.MAX_VALUE);
	}

	public static void execute(Runnable runnable, long maximumRuntimeInMillisecWithoutWarning) {
		try {
			long begin = System.nanoTime();
			runnable.run();
			long runtimeInNanosec = System.nanoTime() - begin;

			if (CommonsConfig.RUNNABLESTATS_ENABLE)
				RunnableStatsManager.handleStats(runnable.getClass(), runtimeInNanosec);

			long runtimeInMillisec = TimeUnit.NANOSECONDS.toMillis(runtimeInNanosec);
			if (runtimeInMillisec > maximumRuntimeInMillisecWithoutWarning && ManagementFactory.getRuntimeMXBean().getUptime() > 60000)
				log.warn(runnable.getClass().getSimpleName() + " - execution time: " + runtimeInMillisec + "ms");
		} catch (Throwable t) {
			log.error("Exception in a Runnable execution:", t);
		}
	}
}
