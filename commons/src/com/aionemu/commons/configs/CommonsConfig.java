package com.aionemu.commons.configs;

import com.aionemu.commons.configuration.Property;

/**
 * @author ATracer
 */
public class CommonsConfig {

	@Property(key = "commons.runnablestats.enable", defaultValue = "false")
	public static boolean RUNNABLESTATS_ENABLE;

	@Property(key = "commons.script_compiler.caching.enable", defaultValue = "true")
	public static volatile boolean SCRIPT_COMPILER_CACHING;
}
