package com.aionemu.gameserver.taskmanager;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import com.aionemu.commons.configs.CommonsConfig;
import com.aionemu.commons.utils.concurrent.RunnableStatsManager;

/**
 * @author NB4L1
 */
public abstract class AbstractIterativePeriodicTaskManager<T> extends AbstractPeriodicTaskManager {

	private final Set<T> startList = new HashSet<>();
	private final Set<T> stopList = new HashSet<>();
	private final Set<T> activeTasks = new LinkedHashSet<>();

	protected AbstractIterativePeriodicTaskManager(int period) {
		super(period);
	}

	public boolean hasTask(T task) {
		readLock();
		try {
			if (stopList.contains(task))
				return false;

			return activeTasks.contains(task) || startList.contains(task);
		} finally {
			readUnlock();
		}
	}

	public void startTask(T task) {
		writeLock();
		try {
			startList.add(task);

			stopList.remove(task);
		} finally {
			writeUnlock();
		}
	}

	public void stopTask(T task) {
		writeLock();
		try {
			stopList.add(task);

			startList.remove(task);
		} finally {
			writeUnlock();
		}
	}

	@Override
	public final void run() {
		writeLock();
		try {
			activeTasks.addAll(startList);
			activeTasks.removeAll(stopList);

			startList.clear();
			stopList.clear();
		} finally {
			writeUnlock();
		}

		for (T task : activeTasks) {
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
	}

	protected abstract void callTask(T task);

	protected abstract String getCalledMethodName();
}
