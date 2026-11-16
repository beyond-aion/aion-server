package com.aionemu.commons.utils.concurrent;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs its tasks one after another in the order they were submitted, on the threads of another executor. It only occupies a thread while it has
 * tasks, so an idle instance costs nothing, and many instances can share one executor (e.g. one per connection on virtual threads).
 * <p>
 * {@link #startWatchdog(Duration)} logs the stack trace of every task that runs longer than the given threshold. It stands in for the
 * {@link DeadLockDetector}, which doesn't see virtual threads.
 */
public class SequentialExecutor implements Executor {

	private static final Logger log = LoggerFactory.getLogger(SequentialExecutor.class);
	private static final Set<SequentialExecutor> instances = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));

	private final Executor executor;
	private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean running = new AtomicBoolean();
	private final AtomicLong finishedTasks = new AtomicLong();
	private volatile Thread runner;

	// only accessed by the watchdog thread
	private Thread watchedRunner;
	private long watchedFinishedTasks;
	private long watchedSinceNanos;
	private boolean reported;

	public SequentialExecutor(Executor executor) {
		this.executor = executor;
		instances.add(this);
	}

	@Override
	public void execute(Runnable task) {
		tasks.add(task);
		if (running.compareAndSet(false, true)) {
			try {
				executor.execute(this::runTasks);
			} catch (RejectedExecutionException e) {
				tasks.remove(task);
				running.set(false);
				throw e;
			}
		}
	}

	private void runTasks() {
		do {
			runner = Thread.currentThread();
			for (Runnable task; (task = tasks.poll()) != null;) {
				try {
					task.run();
				} catch (Throwable t) {
					log.error("Exception in a sequentially executed task", t);
				}
				finishedTasks.lazySet(finishedTasks.get() + 1);
				if (!tasks.isEmpty())
					Thread.yield(); // let tasks of other instances run in between, if they wait for a thread
			}
			runner = null;
			running.set(false);
		} while (!tasks.isEmpty() && running.compareAndSet(false, true));
	}

	/**
	 * Starts a daemon thread that checks all instances every third of the threshold and logs each task once that runs for longer than the threshold.
	 */
	public static void startWatchdog(Duration threshold) {
		Duration checkInterval = threshold.dividedBy(3);
		Thread.ofPlatform().daemon().name("SequentialExecutorWatchdog").start(() -> {
			while (true) {
				try {
					Thread.sleep(checkInterval);
				} catch (InterruptedException e) {
					return;
				}
				checkStuckTasks(System.nanoTime(), threshold);
			}
		});
	}

	/**
	 * @return The number of tasks newly reported as running for longer than the threshold
	 */
	static int checkStuckTasks(long nowNanos, Duration threshold) {
		List<SequentialExecutor> executors;
		synchronized (instances) {
			executors = new ArrayList<>(instances);
		}
		int reportedCount = 0;
		for (SequentialExecutor executor : executors) {
			if (executor.isStuck(nowNanos, threshold.toNanos())) {
				Thread runner = executor.watchedRunner;
				StringBuilder sb = new StringBuilder();
				for (StackTraceElement element : runner.getStackTrace())
					sb.append("\n\tat ").append(element);
				log.warn("A task has been running for more than {} seconds on {}{}", threshold.toSeconds(), runner, sb);
				reportedCount++;
			}
		}
		return reportedCount;
	}

	private boolean isStuck(long nowNanos, long thresholdNanos) {
		Thread currentRunner = runner;
		long finished = finishedTasks.get();
		if (currentRunner == null || currentRunner != watchedRunner || finished != watchedFinishedTasks) {
			watchedRunner = currentRunner;
			watchedFinishedTasks = finished;
			watchedSinceNanos = nowNanos;
			reported = false;
			return false;
		}
		if (reported || nowNanos - watchedSinceNanos < thresholdNanos)
			return false;
		reported = true;
		return true;
	}
}
