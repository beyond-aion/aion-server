package com.aionemu.gameserver.questEngine.model;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

class QuestTimersTest {

	@Test
	void visibleTimerBelongsToOneQuest() {
		QuestTimers timers = new QuestTimers((expiry, delay) -> new FutureTask<>(expiry, null));
		assertTrue(timers.start(2263, true, 300, () -> {}));
		assertFalse(timers.start(2230, true, 1800, () -> {}));
		assertFalse(timers.isRunning(2230));
		assertFalse(timers.cancelVisible(2230));
		assertEquals(2263, timers.getVisibleQuestId());

		assertTrue(timers.start(25082, false, 300, () -> {}));
		assertTrue(timers.isRunning(25082));
		assertTrue(timers.cancelVisible(2263));
		assertTrue(timers.isRunning(25082));

		timers.cancelAll();
		assertFalse(timers.isRunning(25082));
	}

	@Test
	void alreadyRunningVisibleTaskCannotExpireReplacement() throws InterruptedException {
		assertRunningTaskCannotExpireReplacement(2230, true);
	}

	@Test
	void alreadyRunningInvisibleTaskCannotExpireReplacement() throws InterruptedException {
		assertRunningTaskCannotExpireReplacement(25082, false);
	}

	private void assertRunningTaskCannotExpireReplacement(int questId, boolean visible) throws InterruptedException {
		CountDownLatch oldTaskStarted = new CountDownLatch(1);
		CountDownLatch resumeOldTask = new CountDownLatch(1);
		List<FutureTask<Void>> tasks = new ArrayList<>();
		QuestTimers timers = new QuestTimers((expiry, delay) -> {
			boolean first = tasks.isEmpty();
			FutureTask<Void> task = new FutureTask<>(() -> {
				if (first) {
					oldTaskStarted.countDown();
					resumeOldTask.await();
				}
				expiry.run();
				return null;
			});
			tasks.add(task);
			return task;
		});
		AtomicInteger expirations = new AtomicInteger();

		assertTrue(timers.start(questId, visible, 300, expirations::incrementAndGet));
		Thread oldTaskThread = Thread.ofVirtual().start(tasks.getFirst());
		try {
			assertTrue(oldTaskStarted.await(5, TimeUnit.SECONDS));
			assertTrue(timers.start(questId, visible, 300, expirations::incrementAndGet));
			assertTrue(tasks.getFirst().isCancelled());
		} finally {
			resumeOldTask.countDown();
			assertTrue(oldTaskThread.join(Duration.ofSeconds(5)));
		}

		assertEquals(0, expirations.get());
		assertTrue(timers.isRunning(questId));
		assertFalse(tasks.getLast().isCancelled());

		tasks.getLast().run();
		assertEquals(1, expirations.get());
		assertFalse(timers.isRunning(questId));
	}
}
