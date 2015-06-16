package com.aionemu.commons.scripting.scriptmanager;

import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;
import com.aionemu.commons.scripting.scriptmanager.listener.ScheduledTaskClassListenerTestAdapter;
import com.aionemu.commons.services.CronService;
import com.aionemu.commons.services.cron.CurrentThreadRunnableRunner;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.Constructor;

public class ScriptManagerTest extends Assert {

	public static final String SYSTEM_PROPERTY_KEY_CLASS_LOADED = "ScriptManagerClassLoaded";
	public static final String SYSTEM_PROPERTY_KEY_CLASS_UNLOADED = "ScriptManagerClassUnloaded";

	private static final String FILE_TEST_DATA_DIR = "./testdata/scripts/scriptManagerTest";

	private CronService cronService;

	@BeforeClass
	public void initCronService() throws Exception {
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
		assertEquals(System.getProperties().containsKey(SYSTEM_PROPERTY_KEY_CLASS_LOADED), true);

		sm.shutdown();
		assertEquals(System.getProperties().containsKey(SYSTEM_PROPERTY_KEY_CLASS_UNLOADED), true);
	}

	@Test
	public void testScheduledAnnotation() throws Exception{
		ScriptManager sm = new ScriptManager();
		sm.setGlobalClassListener(new ScheduledTaskClassListenerTestAdapter(cronService));
		sm.loadDirectory(new File(FILE_TEST_DATA_DIR));
		assertEquals(cronService.getRunnables().size(), 1);
		sm.shutdown();
		assertEquals(cronService.getRunnables().size(), 0);
	}

	@AfterClass
	public void afterTest() throws Exception {
		cronService.shutdown();
	}
}
