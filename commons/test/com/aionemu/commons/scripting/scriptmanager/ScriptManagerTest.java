package com.aionemu.commons.scripting.scriptmanager;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Constructor;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.aionemu.commons.scripting.scriptmanager.listener.ScheduledTaskClassListenerTestAdapter;
import com.aionemu.commons.services.CronService;
import com.aionemu.commons.services.cron.CurrentThreadRunnableRunner;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;

public class ScriptManagerTest {

	public static final String SYSTEM_PROPERTY_KEY_CLASS_LOADED = "ScriptManagerClassLoaded";
	public static final String SYSTEM_PROPERTY_KEY_CLASS_UNLOADED = "ScriptManagerClassUnloaded";

	private static final String FILE_TEST_DATA_DIR = "./testdata/scripts/scriptManagerTest";

	private static CronService cronService;

	@BeforeClass
	public static void initCronService() throws Exception {
		((Logger) LoggerFactory.getLogger("org.quartz")).setLevel(Level.OFF);
		Constructor<CronService> constructor = CronService.class.getDeclaredConstructor();
		constructor.setAccessible(true);
		cronService = constructor.newInstance();
		cronService.init(CurrentThreadRunnableRunner.class);
	}

	@Test
	public void testOnClassLoadAndUnload() throws Exception {
		ScriptManager sm = new ScriptManager();
		sm.setGlobalClassListener(new OnClassLoadUnloadListener());
		sm.loadDirectory(new File(FILE_TEST_DATA_DIR));
		assertTrue(System.getProperties().containsKey(SYSTEM_PROPERTY_KEY_CLASS_LOADED));

		sm.shutdown();
		assertTrue(System.getProperties().containsKey(SYSTEM_PROPERTY_KEY_CLASS_UNLOADED));
	}

	@Test
	public void testScheduledAnnotation() throws Exception {
		ScriptManager sm = new ScriptManager();
		sm.setGlobalClassListener(new ScheduledTaskClassListenerTestAdapter(cronService));
		sm.loadDirectory(new File(FILE_TEST_DATA_DIR));
		assertEquals(cronService.getRunnables().size(), 1);
		sm.shutdown();
		assertEquals(cronService.getRunnables().size(), 0);
	}

	@AfterClass
	public static void afterTest() throws Exception {
		cronService.shutdown();
	}
}
