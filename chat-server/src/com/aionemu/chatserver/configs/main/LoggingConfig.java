package com.aionemu.chatserver.configs.main;

import com.aionemu.commons.configuration.Property;

public class LoggingConfig {

	/**
	 * Log requests to new channels
	 */
	@Property(key = "chatserver.log.channel.request", defaultValue = "false")
	public static boolean LOG_CHANNEL_REQUEST;

	/**
	 * Log requests to invalid channels
	 */
	@Property(key = "chatserver.log.channel.invalid", defaultValue = "false")
	public static boolean LOG_CHANNEL_INVALID;

	/**
	 * Log Chat
	 */
	@Property(key = "chatserver.log.chat", defaultValue = "false")
	public static boolean LOG_CHAT;

	/**
	 * Log Chat and Save to Database
	 */
	@Property(key = "chatserver.log.chat_to_db", defaultValue = "false")
	public static boolean LOG_CHAT_TO_DB;
}
