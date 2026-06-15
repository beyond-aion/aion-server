package com.aionemu.commons.scripting;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.Test;

import com.aionemu.commons.scripting.classlistener.OnClassLoadUnloadListener;

public class ScriptManagerTest {

	public static final String SYSTEM_PROPERTY_KEY_CLASS_LOADED = "ScriptManagerClassLoaded";
	public static final String SYSTEM_PROPERTY_KEY_CLASS_UNLOADED = "ScriptManagerClassUnloaded";

	private static final File FILE_TEST_DATA_DIR = new File("./testdata/scripts/scriptManagerTest");

	@Test
	public void testOnClassLoadAndUnload() {
		ScriptManager sm = new ScriptManager();
		sm.setGlobalClassListener(new OnClassLoadUnloadListener());
		sm.load(FILE_TEST_DATA_DIR);
		assertTrue(System.getProperties().containsKey(SYSTEM_PROPERTY_KEY_CLASS_LOADED));

		sm.shutdown();
		assertTrue(System.getProperties().containsKey(SYSTEM_PROPERTY_KEY_CLASS_UNLOADED));
	}
}
