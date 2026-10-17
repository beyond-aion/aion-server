package com.aionemu.commons.utils.concurrent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

public class SequentialExecutorTest {

	@Test
	public void runsTasksInOrderAndNeverConcurrently() throws Exception {
		try (ExecutorService threads = Executors.newVirtualThreadPerTaskExecutor()) {
			int executorCount = 50, tasksPerExecutor = 2000;
			List<SequentialExecutor> executors = new ArrayList<>();
			List<List<Integer>> results = new ArrayList<>();
			AtomicInteger[] running = new AtomicInteger[executorCount];
			AtomicInteger overlaps = new AtomicInteger();
			CountDownLatch done = new CountDownLatch(executorCount * tasksPerExecutor);
			for (int i = 0; i < executorCount; i++) {
				executors.add(new SequentialExecutor(threads));
				results.add(new ArrayList<>());
				running[i] = new AtomicInteger();
			}
			for (int task = 0; task < tasksPerExecutor; task++) {
				for (int i = 0; i < executorCount; i++) {
					int executor = i, value = task;
					executors.get(i).execute(() -> {
						if (running[executor].incrementAndGet() != 1)
							overlaps.incrementAndGet();
						results.get(executor).add(value);
						running[executor].decrementAndGet();
						done.countDown();
					});
				}
			}
			assertTrue(done.await(30, TimeUnit.SECONDS));
			assertEquals(0, overlaps.get(), "tasks of one executor overlapped");
			for (List<Integer> result : results) {
				assertEquals(tasksPerExecutor, result.size());
				for (int i = 0; i < tasksPerExecutor; i++)
					assertEquals(i, result.get(i));
			}
		}
	}

	@Test
	public void keepsRunningAfterAFailingTask() throws Exception {
		try (ExecutorService threads = Executors.newVirtualThreadPerTaskExecutor()) {
			SequentialExecutor executor = new SequentialExecutor(threads);
			CountDownLatch done = new CountDownLatch(1);
			executor.execute(() -> {
				throw new IllegalStateException("test");
			});
			executor.execute(done::countDown);
			assertTrue(done.await(5, TimeUnit.SECONDS));
		}
	}

	@Test
	public void dropsARejectedTaskAndAcceptsTasksAgain() {
		AtomicBoolean reject = new AtomicBoolean(true);
		List<Runnable> started = new ArrayList<>();
		SequentialExecutor executor = new SequentialExecutor(r -> {
			if (reject.getAndSet(false))
				throw new RejectedExecutionException();
			started.add(r);
		});
		List<String> ran = new ArrayList<>();
		assertThrows(RejectedExecutionException.class, () -> executor.execute(() -> ran.add("rejected")));
		executor.execute(() -> ran.add("accepted"));
		assertEquals(1, started.size());
		started.getFirst().run();
		assertEquals(List.of("accepted"), ran);
	}
}
