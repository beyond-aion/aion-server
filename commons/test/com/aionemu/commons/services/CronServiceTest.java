package com.aionemu.commons.services;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.TimeZone;
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
		Constructor<CronService> constructor = CronService.class.getDeclaredConstructor(Class.class, TimeZone.class);
		constructor.setAccessible(true);
		cronService = constructor.newInstance(CurrentThreadRunnableRunner.class, null);
	}

	@Test
	public void testCronTriggerExecutionTime() {
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
		assertTrue(cronService.findJobDetails(test).size() == 1);
	}

	@Test
	public void testCancelRunnableUsingRunnableReference() {
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
	public void testCancelRunnableUsingJobDetails() {
		final AtomicInteger val = new AtomicInteger();
		Runnable test = new Runnable() {

			@Override
			public void run() {
				val.getAndIncrement();
				cronService.cancel(cronService.findJobDetails(this).get(0));
			}
		};
		cronService.schedule(test, "0/2 * * * * ?");
		sleep(5);
		assertEquals(val.intValue(), 1);
	}

	@Test
	public void testGetJobTriggers() {
		Runnable r = newRunnable();
		cronService.schedule(r, "0 15 * * * ?");
		JobDetail jd = cronService.findJobDetails(r).get(0);
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
