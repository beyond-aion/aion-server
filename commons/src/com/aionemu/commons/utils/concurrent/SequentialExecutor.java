package com.aionemu.commons.utils.concurrent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs its tasks one after another in the order they were submitted, on the threads of another executor. It only occupies a thread while it has
 * tasks, so an idle instance costs nothing, and many instances can share one executor (e.g. one per connection on virtual threads).
 */
public class SequentialExecutor implements Executor {

	private static final Logger log = LoggerFactory.getLogger(SequentialExecutor.class);

	private final Executor executor;
	private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
	private final AtomicBoolean running = new AtomicBoolean();

	public SequentialExecutor(Executor executor) {
		this.executor = executor;
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
			for (Runnable task; (task = tasks.poll()) != null;) {
				try {
					task.run();
				} catch (Throwable t) {
					log.error("Exception in a sequentially executed task", t);
				}
				if (!tasks.isEmpty())
					Thread.yield(); // let tasks of other instances run in between, if they wait for a thread
			}
			running.set(false);
		} while (!tasks.isEmpty() && running.compareAndSet(false, true));
	}
}
