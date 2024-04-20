package com.aionemu.gameserver.taskmanager;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.aionemu.commons.configs.CommonsConfig;
import com.aionemu.commons.utils.concurrent.RunnableStatsManager;

/**
 * @author lord_rex and MrPoke (based on l2j-free engines), Neon
 */
public abstract class AbstractFIFOPeriodicTaskManager<T> extends AbstractPeriodicTaskManager {

	private final Queue<T> tasks = new ConcurrentLinkedQueue<>();
	private int counter = 0;

	public AbstractFIFOPeriodicTaskManager(int period) {
		super(period);
	}

	public final void add(T t) {
		tasks.add(t);
	}

	@Override
	public final void run() {
		int processedTasks = tasks.size();
		for (int i = processedTasks; i > 0; --i) {
			T task = tasks.poll();
			if (task == null) // no tasks left
				break;

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
		if (tasks.size() <= processedTasks)
			counter = 0;
		else if (++counter == 5) // log error if the pending task queue size increased 5 times in a row
			log.warn("Tasks for " + getClass().getSimpleName() + " are added faster than they can be executed.");
	}

	protected abstract void callTask(T task);

	protected abstract String getCalledMethodName();
}
