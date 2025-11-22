package com.aionemu.gameserver.services.cron;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.quartz.JobDetail;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

/**
 * @author SoulKeeper
 */
class CronServiceTest {

	@BeforeAll
	static void init() {
		((Logger) LoggerFactory.getLogger("org.quartz")).setLevel(Level.OFF);
		CronService.initSingleton(CurrentThreadRunnableRunner.class, null);
	}

	@Test
	void testJobActuallyStarting() throws InterruptedException {
		CountDownLatch latch = new CountDownLatch(1);
		CronService.getInstance().schedule(latch::countDown, "* * * * * ?");
		assertTrue(latch.await(1, TimeUnit.SECONDS));
	}

	@Test
	void testFindJobDetails() {
		Runnable test = () -> {};
		CronService.getInstance().schedule(test, "* * * * * ?");
		assertEquals(1, CronService.getInstance().findJobDetails(test).size());
	}

	@Test
	void testCancelTaskByRunnableReference() {
		Runnable test = () -> {};
		CronService.getInstance().schedule(test, "* * * * * ?");
		assertTrue(CronService.getInstance().cancel(test));
	}

	@Test
	void testCancelTaskByJobDetails() {
		JobDetail jobDetail = CronService.getInstance().schedule(() -> {}, "* * * * * ?");
		assertTrue(CronService.getInstance().cancel(jobDetail));
	}

	@Test
	void testGetJobTriggers() {
		JobDetail jobDetail = CronService.getInstance().schedule(() -> {}, "* * * * * ?");
		assertEquals(1, CronService.getInstance().getJobTriggers(jobDetail).size());
	}

	@AfterAll
	static void shutdown() {
		CronService.getInstance().shutdown();
	}
}