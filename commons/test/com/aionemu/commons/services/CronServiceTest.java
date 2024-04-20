package com.aionemu.commons.services;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
	public void testJobActuallyStarting() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		CronService.getInstance().schedule(latch::countDown, "* * * * * ?");
		assertTrue(latch.await(1, TimeUnit.SECONDS));
	}

	@Test
	public void testFindJobDetails() {
		Runnable test = () -> {};
		CronService.getInstance().schedule(test, "* * * * * ?");
		assertEquals(1, CronService.getInstance().findJobDetails(test).size());
	}

	@Test
	public void testCancelTaskByRunnableReference() {
		Runnable test = () -> {};
		CronService.getInstance().schedule(test, "* * * * * ?");
		assertTrue(CronService.getInstance().cancel(test));
	}

	@Test
	public void testCancelTaskByJobDetails() {
		JobDetail jobDetail = CronService.getInstance().schedule(() -> {}, "* * * * * ?");
		assertTrue(CronService.getInstance().cancel(jobDetail));
	}

	@Test
	public void testGetJobTriggers() {
		JobDetail jobDetail = CronService.getInstance().schedule(() -> {}, "* * * * * ?");
		assertEquals(1, CronService.getInstance().getJobTriggers(jobDetail).size());
	}

	@AfterAll
	public static void shutdown() {
		CronService.getInstance().shutdown();
	}
}
