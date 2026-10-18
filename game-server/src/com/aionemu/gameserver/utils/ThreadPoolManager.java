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

		log.info("ThreadPoolManager initialized with {} instant, {} scheduler and {} long running threads", instantPool.getPoolSize(),
			scheduledPool.getPoolSize(), longRunningPool.getPoolSize());
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
		long begin = System.currentTimeMillis();
		scheduledPool.shutdown();
		instantPool.shutdown();
		longRunningPool.shutdown();

		int tasksLeft = awaitTermination(5000);
		if (tasksLeft == 0)
			log.info("ThreadPoolManager shutdown completed in {} ms", System.currentTimeMillis() - begin);
		else
			log.info("ThreadPoolManager shutdown timed out after {} ms with {} scheduled tasks, {} instant tasks and {} long running tasks left",
				System.currentTimeMillis() - begin, getTaskCount(scheduledPool), getTaskCount(instantPool), getTaskCount(longRunningPool));
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

	private int awaitTermination(long timeoutMillis) {
		for (long expirationTime = System.currentTimeMillis() + timeoutMillis; System.currentTimeMillis() < expirationTime; ) {
			try {
				if (!scheduledPool.awaitTermination(10, TimeUnit.MILLISECONDS))
					continue;
				if (!instantPool.awaitTermination(10, TimeUnit.MILLISECONDS))
					continue;
				if (!longRunningPool.awaitTermination(10, TimeUnit.MILLISECONDS))
					continue;
			} catch (InterruptedException _) {
			}
			break;
		}
		return getTaskCount(scheduledPool) + getTaskCount(instantPool) + getTaskCount(longRunningPool);
	}

	private static final class SingletonHolder {

		private static final ThreadPoolManager INSTANCE = new ThreadPoolManager();
	}

	public static ThreadPoolManager getInstance() {
		return SingletonHolder.INSTANCE;
	}
}
