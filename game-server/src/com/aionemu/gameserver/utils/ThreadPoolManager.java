package com.aionemu.gameserver.utils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.ExitCode;
import com.aionemu.commons.utils.concurrent.AionRejectedExecutionHandler;
import com.aionemu.commons.utils.concurrent.DeadLockDetector;
import com.aionemu.commons.utils.concurrent.PriorityThreadFactory;
import com.aionemu.commons.utils.concurrent.RunnableWrapper;
import com.aionemu.gameserver.configs.main.ThreadConfig;

/**
 * @author -Nemesiss-, NB4L1, MrPoke, lord_rex
 */
public final class ThreadPoolManager implements Executor {

	private static final Logger log = LoggerFactory.getLogger(ThreadPoolManager.class);

	private final ScheduledThreadPoolExecutor scheduledPool;
	private final ThreadPoolExecutor instantPool;
	private final ThreadPoolExecutor longRunningPool;

	private ThreadPoolManager() {
		int availableProcessors = Runtime.getRuntime().availableProcessors();
		int instantPoolSize = Math.max(4, ThreadConfig.BASE_THREAD_POOL_SIZE == 0 ? availableProcessors : ThreadConfig.BASE_THREAD_POOL_SIZE);
		int scheduledPoolSize = Math.max(4, ThreadConfig.SCHEDULED_THREAD_POOL_SIZE == 0 ? availableProcessors : ThreadConfig.SCHEDULED_THREAD_POOL_SIZE);

		DeadLockDetector.start(Duration.ofMinutes(1), () -> System.exit(ExitCode.RESTART));
		instantPool = new ThreadPoolExecutor(instantPoolSize, instantPoolSize, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100000),
			new PriorityThreadFactory("InstantPool", ThreadConfig.USE_PRIORITIES ? 7 : Thread.NORM_PRIORITY));
		instantPool.setRejectedExecutionHandler(new AionRejectedExecutionHandler());
		instantPool.prestartAllCoreThreads();

		scheduledPool = new ScheduledThreadPoolExecutor(scheduledPoolSize);
		scheduledPool.setRejectedExecutionHandler(new AionRejectedExecutionHandler());
		scheduledPool.prestartAllCoreThreads();
		scheduledPool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);

		longRunningPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

		log.info("ThreadPoolManager: Initialized with " + instantPool.getPoolSize() + " instant, " + scheduledPool.getPoolSize() + " scheduler and "
			+ longRunningPool.getPoolSize() + " long running threads");
	}

	public ScheduledFuture<?> schedule(Runnable r, long delay, TimeUnit unit) {
		r = new RunnableWrapper(r, ThreadConfig.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING, true);
		return scheduledPool.schedule(r, delay, unit);
	}

	public ScheduledFuture<?> schedule(Runnable r, long delay) {
		return schedule(r, delay, TimeUnit.MILLISECONDS);
	}

	public ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long delay, long period) {
		r = new RunnableWrapper(r, ThreadConfig.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING, true);
		return scheduledPool.scheduleAtFixedRate(r, delay, period, TimeUnit.MILLISECONDS);
	}

	@Override
	public void execute(Runnable r) {
		instantPool.execute(new RunnableWrapper(r, ThreadConfig.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING, true));
	}

	public void executeLongRunning(Runnable r) {
		longRunningPool.execute(new RunnableWrapper(r));
	}

	public Future<?> submit(Runnable r) {
		return instantPool.submit(new RunnableWrapper(r, ThreadConfig.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING, false));
	}

	public Future<?> submitLongRunning(Runnable r) {
		return longRunningPool.submit(new RunnableWrapper(r, Long.MAX_VALUE, false));
	}

	/**
	 * Shutdown all thread pools.
	 */
	public void shutdown() {
		final long begin = System.currentTimeMillis();

		log.info("ThreadPoolManager: Shutting down.");
		log.info("\t... executing " + scheduledPool.getActiveCount() + "/" + getTaskCount(scheduledPool) + " scheduled tasks.");
		log.info("\t... executing " + getTaskCount(instantPool) + " instant tasks.");
		log.info("\t... executing " + getTaskCount(longRunningPool) + " long running tasks.");

		scheduledPool.shutdown();
		instantPool.shutdown();
		longRunningPool.shutdown();

		boolean success = false;
		try {
			success = awaitTermination(5000);
		} catch (InterruptedException ignored) {
		}

		log.info("\t... success: " + success + " in " + (System.currentTimeMillis() - begin) + " msec.");
		log.info("\t... " + getTaskCount(scheduledPool) + " scheduled tasks left.");
		log.info("\t... " + getTaskCount(instantPool) + " instant tasks left.");
		log.info("\t... " + getTaskCount(longRunningPool) + " long running tasks left.");
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
		list.add("Long running pool:");
		list.add("=================================================");
		list.add("\tgetActiveCount: ...... " + longRunningPool.getActiveCount());
		list.add("\tgetCorePoolSize: ..... " + longRunningPool.getCorePoolSize());
		list.add("\tgetPoolSize: ......... " + longRunningPool.getPoolSize());
		list.add("\tgetLargestPoolSize: .. " + longRunningPool.getLargestPoolSize());
		list.add("\tgetMaximumPoolSize: .. " + longRunningPool.getMaximumPoolSize());
		list.add("\tgetCompletedTaskCount: " + longRunningPool.getCompletedTaskCount());
		list.add("\tgetQueuedTaskCount: .. " + longRunningPool.getQueue().size());
		list.add("\tgetTaskCount: ........ " + longRunningPool.getTaskCount());

		return list;
	}

	private boolean awaitTermination(long timeoutInMillisec) throws InterruptedException {
		final long begin = System.currentTimeMillis();

		while (System.currentTimeMillis() - begin < timeoutInMillisec) {
			if (!scheduledPool.awaitTermination(10, TimeUnit.MILLISECONDS))
				continue;

			if (!instantPool.awaitTermination(10, TimeUnit.MILLISECONDS))
				continue;

			if (!longRunningPool.awaitTermination(10, TimeUnit.MILLISECONDS))
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
