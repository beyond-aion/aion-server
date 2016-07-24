package com.aionemu.gameserver.taskmanager;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.concurrent.RunnableStatsManager;

/**
 * @author lord_rex and MrPoke based on l2j-free engines.
 * @modified Neon
 */
public abstract class AbstractFIFOPeriodicTaskManager<T> extends AbstractPeriodicTaskManager {

	private static final Logger log = LoggerFactory.getLogger(AbstractFIFOPeriodicTaskManager.class);
	private final ConcurrentLinkedQueue<T> tasks = new ConcurrentLinkedQueue<>();
	private int lastPendingTasks = 0;
	private int counter = 0;

	public AbstractFIFOPeriodicTaskManager(int period) {
		super(period);
	}

	public final void add(T t) {
		tasks.add(t);
	}

	@Override
	public final void run() {
		for (int i = tasks.size(); i > 0; --i) {
			T task = tasks.poll();
			if (task == null) // no tasks left
				break;

			final long begin = System.nanoTime();

			try {
				callTask(task);
			} catch (RuntimeException e) {
				log.warn("Exception in " + getClass().getSimpleName(), e);
			} finally {
				RunnableStatsManager.handleStats(task.getClass(), getCalledMethodName(), System.nanoTime() - begin);
			}
		}
		int pendingTasks = tasks.size();
		if (pendingTasks == 0 || pendingTasks < lastPendingTasks)
			counter = 0;
		else if (pendingTasks > lastPendingTasks && ++counter == 5) // log error if the pending task queue size increased 5 times without ever decreasing
			log.error("Tasks for " + getClass().getSimpleName() + " are added faster than they can be executed.");
		lastPendingTasks = pendingTasks;
	}

	protected abstract void callTask(T task);

	protected abstract String getCalledMethodName();
}
