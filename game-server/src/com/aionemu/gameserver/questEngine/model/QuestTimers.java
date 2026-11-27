package com.aionemu.gameserver.questEngine.model;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.function.BiFunction;

import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Owns the visible timer slot and the independent invisible timer of each quest.
 */
public final class QuestTimers {

	private static final int VISIBLE_SLOT = 0;

	private final Map<Integer, Timer> timers = new HashMap<>();
	private final BiFunction<Runnable, Long, Future<?>> schedule;

	public QuestTimers() {
		this((task, delay) -> ThreadPoolManager.getInstance().schedule(task, delay));
	}

	QuestTimers(BiFunction<Runnable, Long, Future<?>> schedule) {
		this.schedule = schedule;
	}

	public synchronized boolean start(int questId, boolean visible, int seconds, Runnable onExpiry) {
		int slot = visible ? VISIBLE_SLOT : questId;
		Timer previous = timers.get(slot);
		if (previous != null && previous.questId != questId)
			return false;

		Timer timer = new Timer(questId);
		timer.task = schedule.apply(() -> expire(slot, timer, onExpiry), seconds * 1000L);
		timers.put(slot, timer);
		if (previous != null)
			previous.task.cancel(false);
		return true;
	}

	public synchronized int getVisibleQuestId() {
		Timer timer = timers.get(VISIBLE_SLOT);
		return timer == null ? 0 : timer.questId;
	}

	public synchronized boolean isRunning(int questId) {
		return getVisibleQuestId() == questId || timers.containsKey(questId);
	}

	public synchronized boolean cancelVisible(int questId) {
		return getVisibleQuestId() == questId && cancel(VISIBLE_SLOT);
	}

	public synchronized void cancelInvisible(int questId) {
		cancel(questId);
	}

	public synchronized void cancelAll() {
		timers.values().forEach(timer -> timer.task.cancel(false));
		timers.clear();
	}

	private boolean cancel(int slot) {
		Timer timer = timers.remove(slot);
		if (timer == null)
			return false;
		timer.task.cancel(false);
		return true;
	}

	private synchronized void expire(int slot, Timer timer, Runnable onExpiry) {
		// Cancellation does not stop an already running task. Finish expiry before another thread can start the next timer.
		if (timers.remove(slot, timer))
			onExpiry.run();
	}

	private static final class Timer {

		private final int questId;
		private Future<?> task;

		private Timer(int questId) {
			this.questId = questId;
		}
	}
}
