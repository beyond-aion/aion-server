package com.aionemu.gameserver.taskmanager;

import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.aionemu.commons.configs.CommonsConfig;
import com.aionemu.commons.utils.concurrent.RunnableStatsManager;

/**
 * @author lord_rex and MrPoke (based on l2j-free engines), Neon
 */
public abstract class AbstractFIFOPeriodicTaskManager<T> extends AbstractPeriodicTaskManager {

	private static final int WARNING_PERIOD_SECONDS = 10;
	private final Queue<T> tasks = new ConcurrentLinkedQueue<>();
	private final Set<T> processedTasks = new LinkedHashSet<>();
	private final int counterLimit;
	private int counter = 0;

	public AbstractFIFOPeriodicTaskManager(int periodMillis) {
		super(periodMillis);
		counterLimit = Math.max(5, WARNING_PERIOD_SECONDS * 1000 / periodMillis);
	}

	public final void add(T t) {
		tasks.add(t);
	}

	@Override
	public synchronized final void run() {
		int previouslyProcessedTasksSize = processedTasks.size();
		processedTasks.clear();
		for (int i = tasks.size(); i > 0; --i) {
			T task = tasks.poll();
			if (task == null) // no tasks left
				break;
			processedTasks.add(task);
		}
		for (T task : processedTasks) {
			try {
				long begin = System.nanoTime();
				callTask(task);
				if (CommonsConfig.RUNNABLESTATS_ENABLE) {
					long duration = System.nanoTime() - begin;
					RunnableStatsManager.handleStats(task.getClass(), getCalledMethodName(), duration);
				}
			} catch (Exception e) {
				log.error("Exception in " + getClass().getSimpleName() + " processing " + task, e);
			}
		}
		if (processedTasks.size() <= previouslyProcessedTasksSize)
			counter = 0;
		else if (++counter % counterLimit == 0) // log warning if the task queue size continually increased over the last WARNING_PERIOD_SECONDS
			log.warn("Tasks for " + getClass().getSimpleName() + " are added faster than they can be executed (currently " + processedTasks.size() + " tasks).");
	}

	protected abstract void callTask(T task);

	protected abstract String getCalledMethodName();
}
