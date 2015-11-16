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

	protected static final Logger log = LoggerFactory.getLogger(AbstractFIFOPeriodicTaskManager.class);

	private final ConcurrentLinkedQueue<T> tasks = new ConcurrentLinkedQueue<>();

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
			if (task == null)
				break;

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
