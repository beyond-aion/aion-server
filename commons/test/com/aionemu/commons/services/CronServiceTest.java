package com.aionemu.commons.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.quartz.JobDetail;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.services.cron.CurrentThreadRunnableRunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * @author SoulKeeper
 */
public class CronServiceTest {

	@BeforeAll
	public static void init() {
		((Logger) LoggerFactory.getLogger("org.quartz")).setLevel(Level.OFF);
		CronService.initSingleton(CurrentThreadRunnableRunner.class, null);
	}

	@Test
	public void testCronTriggerExecutionTime() throws InterruptedException {
		AtomicInteger ref = new AtomicInteger();
		// should run on second # 0 and every 2 seconds
		// execute on 0, 2, 4...
		CronService.getInstance().schedule(ref::getAndIncrement, "0/2 * * * * ?");
		Thread.sleep(5000);
		assertEquals(3, ref.intValue());
	}

	@Test
	public void testGetRunnable() {
		Runnable test = () -> {};
		CronService.getInstance().schedule(test, "* 5 * * * ?");
		assertEquals(1, CronService.getInstance().findJobDetails(test).size());
	}

	@Test
	public void testCancelRunnableUsingRunnableReference() throws InterruptedException {
		AtomicInteger val = new AtomicInteger();
		Runnable test = new Runnable() {

			@Override
			public void run() {
				val.getAndIncrement();
				CronService.getInstance().cancel(this);
			}
		};
		CronService.getInstance().schedule(test, "0/2 * * * * ?");
		Thread.sleep(5000);
		assertEquals(1, val.intValue());
	}

	@Test
	public void testCancelRunnableUsingJobDetails() throws InterruptedException {
		AtomicInteger val = new AtomicInteger();
		Runnable test = new Runnable() {

			@Override
			public void run() {
				val.getAndIncrement();
				CronService.getInstance().cancel(CronService.getInstance().findJobDetails(this).get(0));
			}
		};
		CronService.getInstance().schedule(test, "0/2 * * * * ?");
		Thread.sleep(5000);
		assertEquals(1, val.intValue());
	}

	@Test
	public void testGetJobTriggers() {
		Runnable r = () -> {};
		CronService.getInstance().schedule(r, "0 15 * * * ?");
		JobDetail jd = CronService.getInstance().findJobDetails(r).get(0);
		assertEquals(1, CronService.getInstance().getJobTriggers(jd).size());
	}

	@AfterAll
	public static void shutdown() {
		CronService.getInstance().shutdown();
	}
}
