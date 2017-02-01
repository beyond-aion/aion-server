package com.aionemu.gameserver.configs.main;

import com.aionemu.commons.configuration.Property;

public class LoggingConfig {

	/**
	 * Logging
	 */
	@Property(key = "gameserver.log.audit", defaultValue = "true")
	public static boolean LOG_AUDIT;

	@Property(key = "gameserver.log.craft", defaultValue = "true")
	public static boolean LOG_CRAFT;

	@Property(key = "gameserver.log.gmaudit", defaultValue = "true")
	public static boolean LOG_GMAUDIT;

	@Property(key = "gameserver.log.chats.general", defaultValue = "true")
	public static boolean LOG_GENERAL_CHATS;

	@Property(key = "gameserver.log.chats.private", defaultValue = "false")
	public static boolean LOG_PRIVATE_CHATS;

	@Property(key = "gameserver.log.ingameshop", defaultValue = "false")
	public static boolean LOG_INGAMESHOP;

	@Property(key = "gameserver.log.ingameshop.sql", defaultValue = "false")
	public static boolean LOG_INGAMESHOP_SQL;

	@Property(key = "gameserver.log.item", defaultValue = "true")
	public static boolean LOG_ITEM;

	@Property(key = "gameserver.log.kill", defaultValue = "false")
	public static boolean LOG_KILL;

	@Property(key = "gameserver.log.pl", defaultValue = "false")
	public static boolean LOG_PL;

	@Property(key = "gameserver.log.mail", defaultValue = "false")
	public static boolean LOG_MAIL;

	@Property(key = "gameserver.log.player.exchange", defaultValue = "false")
	public static boolean LOG_PLAYER_EXCHANGE;

	@Property(key = "gameserver.log.broker.exchange", defaultValue = "false")
	public static boolean LOG_BROKER_EXCHANGE;

	@Property(key = "gameserver.log.siege", defaultValue = "false")
	public static boolean LOG_SIEGE;

	@Property(key = "gameserver.log.sysmail", defaultValue = "false")
	public static boolean LOG_SYSMAIL;

	@Property(key = "gameserver.log.auction", defaultValue = "true")
	public static boolean LOG_HOUSE_AUCTION;

	@Property(key = "gameserver.log.tampering", defaultValue = "false")
	public static boolean LOG_TAMPERING;
}
