package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

/**
 * @author lord_rex
 */
public class HTMLConfig {

	/**
	 * Enable HTML Welcome Message
	 */
	@Property(key = "gameserver.html.welcome.enable", defaultValue = "false")
	public static boolean ENABLE_HTML_WELCOME;

	/**
	 * Enable HTML Guide Message
	 */
	@Property(key = "gameserver.html.guides.enable", defaultValue = "false")
	public static boolean ENABLE_GUIDES;

	/**
	 * Html files directory
	 */
	@Property(key = "gameserver.html.root", defaultValue = "./data/static_data/HTML/")
	public static String HTML_ROOT;

	/**
	 * Html cache directory
	 */
	@Property(key = "gameserver.html.cache.file", defaultValue = "./cache/html.cache")
	public static String HTML_CACHE_FILE;

	/**
	 * Encoding
	 */
	@Property(key = "gameserver.html.encoding", defaultValue = "UTF-8")
	public static String HTML_ENCODING;
}
