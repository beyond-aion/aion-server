package com.aionemu.commons.utils.concurrent;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.jupiter.api.Test;

public class RunnableStatsManagerTest {

	/**
	 * The network threads record the write time of each server packet, the packet threads the run time of each client packet, so new classes and
	 * methods get registered by several threads at once.
	 */
	@Test
	public void recordsStatsFromManyThreadsAtOnce() throws Exception {
		Class<?>[] classes = { String.class, Integer.class, Long.class, Double.class, List.class, ArrayList.class, Thread.class, Runnable.class };
		CountDownLatch start = new CountDownLatch(1);
		List<Future<?>> futures = new ArrayList<>();
		try (ExecutorService threads = Executors.newFixedThreadPool(16)) {
			for (int t = 0; t < 16; t++) {
				int thread = t;
				futures.add(threads.submit(() -> {
					start.await();
					for (int i = 0; i < 20_000; i++)
						RunnableStatsManager.handleStats(classes[(i + thread) % classes.length], "method" + (i % 50) + "()", i);
					return null;
				}));
			}
			start.countDown();
			for (Future<?> f : futures)
				f.get(); // rethrows what a thread threw
		}
	}
}
