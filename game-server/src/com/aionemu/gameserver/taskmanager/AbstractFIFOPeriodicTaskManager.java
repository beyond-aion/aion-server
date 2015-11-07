package com.aionemu.gameserver.taskmanager;

import java.util.Set;

import javolution.util.FastSet;
import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.concurrent.RunnableStatsManager;

/**
 * @author lord_rex and MrPoke based on l2j-free engines.
 * @modified Neon
 */
public abstract class AbstractFIFOPeriodicTaskManager<T> extends AbstractPeriodicTaskManager {

	protected static final Logger log = LoggerFactory.getLogger(AbstractFIFOPeriodicTaskManager.class);

	private final Set<T> queue = new FastSet<T>(); // set (keeps insertion order) to avoid duplicate tasks
	private final FastTable<T> activeTasks = new FastTable<T>().atomic(); // atomic deque in case a thread isn't done when the new one starts

	public AbstractFIFOPeriodicTaskManager(int period) {
		super(period);
	}

	public final void add(T t) {
		writeLock();
		try {
			queue.add(t);
		} finally {
			writeUnlock();
		}
	}

	@Override
	public final void run() {
		writeLock();
		try {
			activeTasks.addAll(queue);
			queue.clear();
		} finally {
			writeUnlock();
		}

		while (!activeTasks.isEmpty()) {
			T task = activeTasks.pollFirst();
			final long begin = System.nanoTime();

			try {
				callTask(task);
			} catch (RuntimeException e) {
				log.warn("", e);
			} finally {
				RunnableStatsManager.handleStats(task.getClass(), getCalledMethodName(), System.nanoTime() - begin);
			}
		}
	}

	protected abstract void callTask(T task);

	protected abstract String getCalledMethodName();
}
