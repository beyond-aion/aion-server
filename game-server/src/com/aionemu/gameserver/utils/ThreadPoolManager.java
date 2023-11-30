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
	private final ForkJoinPool workStealingPool;

	private ThreadPoolManager() {
		int instantPoolSize = ThreadConfig.BASE_THREAD_POOL_SIZE == 0 ? Runtime.getRuntime().availableProcessors() : ThreadConfig.BASE_THREAD_POOL_SIZE;
		int scheduledPoolSize = ThreadConfig.SCHEDULED_THREAD_POOL_SIZE == 0 ? Runtime.getRuntime().availableProcessors() * 4 : ThreadConfig.SCHEDULED_THREAD_POOL_SIZE;
		// common ForkJoin (for .parallelStream() calls) uses the calling thread too, so we need to subtract 1 to use exactly the number of threads desired
		System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(instantPoolSize - 1));

		DeadLockDetector.start(Duration.ofMinutes(1), () -> System.exit(ExitCode.RESTART));
		instantPool = new ThreadPoolExecutor(instantPoolSize, instantPoolSize, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100000),
			new PriorityThreadFactory("InstantPool", ThreadConfig.USE_PRIORITIES ? 7 : Thread.NORM_PRIORITY));
		instantPool.setRejectedExecutionHandler(new AionRejectedExecutionHandler());
		instantPool.prestartAllCoreThreads();

		scheduledPool = new ScheduledThreadPoolExecutor(scheduledPoolSize);
		scheduledPool.setRejectedExecutionHandler(new AionRejectedExecutionHandler());
		scheduledPool.prestartAllCoreThreads();

		longRunningPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
		longRunningPool.setRejectedExecutionHandler(new AionRejectedExecutionHandler());
		longRunningPool.prestartAllCoreThreads();

		WorkStealThreadFactory forkJoinThreadFactory = new WorkStealThreadFactory("ForkJoinPool");
		workStealingPool = new ForkJoinPool(instantPoolSize, forkJoinThreadFactory, null, true);

		Thread maintainThread = Thread.ofVirtual().name("ThreadPool Purge Task").unstarted(this::purge);
		scheduleAtFixedRate(maintainThread, 150000, 150000);

		log.info("ThreadPoolManager: Initialized with " + instantPool.getPoolSize() + " instant, " + scheduledPool.getPoolSize() + " scheduler, "
			+ longRunningPool.getPoolSize() + " long running, and " + workStealingPool.getPoolSize() + " forking thread(s).");
	}

	public final ScheduledFuture<?> schedule(Runnable r, long delay, TimeUnit unit) {
		r = new RunnableWrapper(r, ThreadConfig.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING, true);
		return scheduledPool.schedule(r, delay, unit);
	}

	public final ScheduledFuture<?> schedule(Runnable r, long delay) {
		return schedule(r, delay, TimeUnit.MILLISECONDS);
	}

	public final ScheduledFuture<?> scheduleAtFixedRate(Runnable r, long delay, long period) {
		r = new RunnableWrapper(r, ThreadConfig.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING, true);
		return scheduledPool.scheduleAtFixedRate(r, delay, period, TimeUnit.MILLISECONDS);
	}

	public ForkJoinPool getForkingPool() {
		return workStealingPool;
	}

	@Override
	public final void execute(Runnable r) {
		instantPool.execute(new RunnableWrapper(r, ThreadConfig.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING, true));
	}

	public final void executeLongRunning(Runnable r) {
		longRunningPool.execute(new RunnableWrapper(r));
	}

	public final Future<?> submit(Runnable r) {
		return instantPool.submit(new RunnableWrapper(r, ThreadConfig.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING, false));
	}

	public final Future<?> submitLongRunning(Runnable r) {
		return longRunningPool.submit(new RunnableWrapper(r, Long.MAX_VALUE, false));
	}

	public void purge() {
		scheduledPool.purge();
		instantPool.purge();
		longRunningPool.purge();
		// workStealingPool is already maintaining needed threads
	}

	/**
	 * Shutdown all thread pools.
	 */
	public void shutdown() {
		final long begin = System.currentTimeMillis();

		log.info("ThreadPoolManager: Shutting down.");
		log.info("\t... executing " + getTaskCount(scheduledPool) + " scheduled tasks.");
		log.info("\t... executing " + getTaskCount(instantPool) + " instant tasks.");
		log.info("\t... executing " + getTaskCount(longRunningPool) + " long running tasks.");
		log.info("\t... " + (workStealingPool.getQueuedTaskCount() + workStealingPool.getQueuedSubmissionCount()) + " forking tasks left.");

		scheduledPool.shutdown();
		instantPool.shutdown();
		longRunningPool.shutdown();
		workStealingPool.shutdown();

		boolean success = false;
		try {
			success = awaitTermination(5000);

			scheduledPool.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
			scheduledPool.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);

			success |= awaitTermination(10000);
		} catch (InterruptedException ignored) {
		}

		log.info("\t... success: " + success + " in " + (System.currentTimeMillis() - begin) + " msec.");
		log.info("\t... " + getTaskCount(scheduledPool) + " scheduled tasks left.");
		log.info("\t... " + getTaskCount(instantPool) + " instant tasks left.");
		log.info("\t... " + getTaskCount(longRunningPool) + " long running tasks left.");
		log.info("\t... " + (workStealingPool.getQueuedTaskCount() + workStealingPool.getQueuedSubmissionCount()) + " forking tasks left.");
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
		list.add("");
		list.add("Work forking pool:");
		list.add("=================================================");
		list.add("\tgetActiveCount: ...... " + workStealingPool.getActiveThreadCount());
		list.add("\tgetPoolSize: ......... " + workStealingPool.getPoolSize());
		list.add("\tgetStealCount: ........" + workStealingPool.getStealCount());
		list.add("\tgetQueuedTaskCount: .. " + workStealingPool.getQueuedTaskCount());
		list.add("\tgetRunningThreadCount: " + workStealingPool.getRunningThreadCount());

		return list;
	}

	private boolean awaitTermination(long timeoutInMillisec) throws InterruptedException {
		final long begin = System.currentTimeMillis();

		while (System.currentTimeMillis() - begin < timeoutInMillisec) {
			if (!scheduledPool.awaitTermination(10, TimeUnit.MILLISECONDS) && scheduledPool.getActiveCount() > 0)
				continue;

			if (!instantPool.awaitTermination(10, TimeUnit.MILLISECONDS) && instantPool.getActiveCount() > 0)
				continue;

			if (!workStealingPool.awaitTermination(10, TimeUnit.MILLISECONDS) && workStealingPool.getActiveThreadCount() > 0)
				continue;

			if (!longRunningPool.awaitTermination(10, TimeUnit.MILLISECONDS) && longRunningPool.getActiveCount() > 0)
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
