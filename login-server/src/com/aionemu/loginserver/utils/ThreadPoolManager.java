package com.aionemu.loginserver.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.concurrent.AionRejectedExecutionHandler;
import com.aionemu.commons.utils.concurrent.DeadLockDetector;
import com.aionemu.commons.utils.concurrent.RunnableWrapper;
import com.aionemu.commons.utils.concurrent.ScheduledFutureWrapper;

/**
 * @author -Nemesiss-, NB4L1, MrPoke, lord_rex
 */
public final class ThreadPoolManager implements Executor {

	private static final Logger log = LoggerFactory.getLogger(ThreadPoolManager.class);

	public static final long MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING = 5000;

	private final ScheduledThreadPoolExecutor scheduledPool;
	private final ThreadPoolExecutor instantPool;

	private ThreadPoolManager() {
		int logicalCoreCount = Runtime.getRuntime().availableProcessors();

		new DeadLockDetector(60, DeadLockDetector.RESTART).start();
		scheduledPool = new ScheduledThreadPoolExecutor(logicalCoreCount * 2);
		scheduledPool.setRejectedExecutionHandler(new AionRejectedExecutionHandler());
		scheduledPool.prestartAllCoreThreads();

		instantPool = new ThreadPoolExecutor(logicalCoreCount, logicalCoreCount, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100000));
		instantPool.setRejectedExecutionHandler(new AionRejectedExecutionHandler());
		instantPool.prestartAllCoreThreads();

		scheduleAtFixedRate(this::purge, 150000, 150000);

		log.info("ThreadPoolManager: Initialized with " + scheduledPool.getPoolSize() + " scheduler and " + instantPool.getPoolSize() + " instant threads.");
	}

	public final ScheduledFuture<?> schedule(Runnable r, long delay) {
		r = new RunnableWrapper(r, MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING, true);
		return new ScheduledFutureWrapper(scheduledPool.schedule(r, delay, TimeUnit.MILLISECONDS));
	}

	public final ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long delay, long period) {
		r = new RunnableWrapper(r, MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING, true);
		return new ScheduledFutureWrapper(scheduledPool.scheduleAtFixedRate(r, delay, period, TimeUnit.MILLISECONDS));
	}

	@Override
	public final void execute(Runnable r) {
		r = new RunnableWrapper(r, MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING, true);
		instantPool.execute(r);
	}

	public final Future<?> submit(Runnable r) {
		r = new RunnableWrapper(r, MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING, false);
		return instantPool.submit(r);
	}

	private void purge() {
		scheduledPool.purge();
		instantPool.purge();
	}

	/**
	 * Shutdown all thread pools.
	 */
	public void shutdown() {
		final long begin = System.currentTimeMillis();

		log.info("ThreadPoolManager: Shutting down.");
		log.info("\t... executing " + getTaskCount(scheduledPool) + " scheduled tasks.");
		log.info("\t... executing " + getTaskCount(instantPool) + " instant tasks.");

		scheduledPool.shutdown();
		instantPool.shutdown();

		boolean success = false;
		try {
			success = awaitTermination(5000);

			scheduledPool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
			scheduledPool.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

			success |= awaitTermination(10000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		log.info("\t... success: " + success + " in " + (System.currentTimeMillis() - begin) + " msec.");
		log.info("\t... " + getTaskCount(scheduledPool) + " scheduled tasks left.");
		log.info("\t... " + getTaskCount(instantPool) + " instant tasks left.");
	}

	private int getTaskCount(ThreadPoolExecutor tp) {
		return tp.getQueue().size() + tp.getActiveCount();
	}

	public List<String> getStats() {
		List<String> list = new ArrayList<>();

		list.add("");
		list.add("Scheduled pool:");
		list.add("=================================================");
		list.add("\tgetActiveCount: ...... " + scheduledPool.getActiveCount());
		list.add("\tgetCorePoolSize: ..... " + scheduledPool.getCorePoolSize());
		list.add("\tgetPoolSize: ......... " + scheduledPool.getPoolSize());
		list.add("\tgetLargestPoolSize: .. " + scheduledPool.getLargestPoolSize());
		list.add("\tgetMaximumPoolSize: .. " + scheduledPool.getMaximumPoolSize());
		list.add("\tgetCompletedTaskCount: " + scheduledPool.getCompletedTaskCount());
		list.add("\tgetQueuedTaskCount: .. " + scheduledPool.getQueue().size());
		list.add("\tgetTaskCount: ........ " + scheduledPool.getTaskCount());
		list.add("");
		list.add("Instant pool:");
		list.add("=================================================");
		list.add("\tgetActiveCount: ...... " + instantPool.getActiveCount());
		list.add("\tgetCorePoolSize: ..... " + instantPool.getCorePoolSize());
		list.add("\tgetPoolSize: ......... " + instantPool.getPoolSize());
		list.add("\tgetLargestPoolSize: .. " + instantPool.getLargestPoolSize());
		list.add("\tgetMaximumPoolSize: .. " + instantPool.getMaximumPoolSize());
		list.add("\tgetCompletedTaskCount: " + instantPool.getCompletedTaskCount());
		list.add("\tgetQueuedTaskCount: .. " + instantPool.getQueue().size());
		list.add("\tgetTaskCount: ........ " + instantPool.getTaskCount());
		list.add("");

		return list;
	}

	private boolean awaitTermination(long timeoutInMillisec) throws InterruptedException {
		final long begin = System.currentTimeMillis();

		while (System.currentTimeMillis() - begin < timeoutInMillisec) {
			if (!scheduledPool.awaitTermination(10, TimeUnit.MILLISECONDS) && scheduledPool.getActiveCount() > 0)
				continue;

			if (!instantPool.awaitTermination(10, TimeUnit.MILLISECONDS) && instantPool.getActiveCount() > 0)
				continue;

			return true;
		}

		return false;
	}

	private static final class SingletonHolder {

		private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();
	}

	public static ThreadPoolManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
}
