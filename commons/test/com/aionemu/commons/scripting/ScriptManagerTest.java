package com.aionemu.commons.scripting;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.TimeZone;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.aionemu.commons.scripting.listener.ScheduledTaskClassListenerTestAdapter;
import com.aionemu.commons.services.CronService;
import com.aionemu.commons.services.cron.CurrentThreadRunnableRunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class ScriptManagerTest {

	public static final String SYSTEM_PROPERTY_KEY_CLASS_LOADED = "ScriptManagerClassLoaded";
	public static final String SYSTEM_PROPERTY_KEY_CLASS_UNLOADED = "ScriptManagerClassUnloaded";

	private static final File FILE_TEST_DATA_DIR = new File("./testdata/scripts/scriptManagerTest");

	private static CronService cronService;

	@BeforeAll
	public static void initCronService() throws Exception {
		((Logger) LoggerFactory.getLogger("org.quartz")).setLevel(Level.OFF);
		Constructor<CronService> constructor = CronService.class.getDeclaredConstructor(Class.class, TimeZone.class);
		constructor.setAccessible(true);
		cronService = constructor.newInstance(CurrentThreadRunnableRunner.class, null);
	}

	@Test
	public void testOnClassLoadAndUnload() {
		ScriptManager sm = new ScriptManager();
		sm.setGlobalClassListener(new OnClassLoadUnloadListener());
		sm.load(FILE_TEST_DATA_DIR);
		assertTrue(System.getProperties().containsKey(SYSTEM_PROPERTY_KEY_CLASS_LOADED));

		sm.shutdown();
		assertTrue(System.getProperties().containsKey(SYSTEM_PROPERTY_KEY_CLASS_UNLOADED));
	}

	@Test
	public void testScheduledAnnotation() {
		ScriptManager sm = new ScriptManager();
		sm.setGlobalClassListener(new ScheduledTaskClassListenerTestAdapter(cronService));
		sm.load(FILE_TEST_DATA_DIR);
		assertEquals(cronService.findJobs(Runnable.class, true).size(), 1);
		sm.shutdown();
		assertEquals(cronService.findJobs(Runnable.class, true).size(), 0);
	}

	@AfterAll
	public static void afterTest() {
		cronService.shutdown();
	}
}
