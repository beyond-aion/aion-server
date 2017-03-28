package com.aionemu.commons.services;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.quartz.JobDetail;
import org.quartz.Trigger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.services.cron.CurrentThreadRunnableRunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * @author SoulKeeper
 */
public class CronServiceTest {

	private static CronService cronService;

	@BeforeClass
	public static void init() throws Exception {
		((Logger) LoggerFactory.getLogger("org.quartz")).setLevel(Level.OFF);
		Constructor<CronService> constructor = CronService.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		cronService = constructor.newInstance();
		cronService.init(CurrentThreadRunnableRunner.class);
	}

	@Test
	public void testCronTriggerExecutionTime() throws Exception {
		AtomicInteger ref = new AtomicInteger();
		Runnable test = newIncrementingRunnable(ref);

		// should run on second # 0 and every 2 seconds
		// execute on 0, 2, 4...

		cronService.schedule(test, "0/2 * * * * ?");
		sleep(5);
		assertEquals(ref.intValue(), 3);
	}

	@Test
	public void testGetRunnables() {
		Runnable test = newRunnable();
		cronService.schedule(test, "* 5 * * * ?");
		Collection<Runnable> col = cronService.getRunnables().keySet();
		assertTrue(col.contains(test));
	}

	@Test
	public void testCancelRunnableUsingRunnableReference() throws Exception {
		final AtomicInteger val = new AtomicInteger();
		Runnable test = new Runnable() {

			@Override
			public void run() {
				val.getAndIncrement();
				cronService.cancel(this);
			}
		};
		cronService.schedule(test, "0/2 * * * * ?");
		sleep(5);
		assertEquals(val.intValue(), 1);
	}

	@Test
	public void testCancelRunnableUsingJobDetails() throws Exception {
		final AtomicInteger val = new AtomicInteger();
		Runnable test = new Runnable() {

			@Override
			public void run() {
				val.getAndIncrement();
				cronService.cancel(cronService.getRunnables().get(this));
			}
		};
		cronService.schedule(test, "0/2 * * * * ?");
		sleep(5);
		assertEquals(val.intValue(), 1);
	}

	@Test
	public void testCancelledRunableGC() throws Exception {
		final AtomicBoolean collected = new AtomicBoolean();
		Runnable r = new Runnable() {

			@Override
			public void run() {
				cronService.cancel(this);
			}

			@Override
			public void finalize() throws Throwable {
				collected.set(true);
				super.finalize();
			}
		};

		cronService.schedule(r, "/1 * * * * ?");
		r = null;
		sleep(1);
		System.gc();
		sleep(1);
		assertTrue(collected.get());
	}

	@Test
	public void testGetJobTriggers() {
		Runnable r = newRunnable();
		cronService.schedule(r, "0 15 * * * ?");
		JobDetail jd = cronService.getRunnables().get(r);
		List<? extends Trigger> triggers = cronService.getJobTriggers(jd);
		assertEquals(triggers.size(), 1);
	}

	@AfterClass
	public static void shutdown() {
		cronService.shutdown();
	}

	private static Runnable newRunnable() {
		return newIncrementingRunnable(null);
	}

	private static Runnable newIncrementingRunnable(final AtomicInteger ref) {
		return new Runnable() {

			@Override
			public void run() {
				if (ref != null) {
					ref.getAndIncrement();
				}
			}
		};
	}

	private static void sleep(int seconds) {
		try {
			Thread.sleep(seconds * 1000);
		} catch (InterruptedException e) {
			throw new RuntimeException("Sleep Interrupted", e);
		}
	}
}
