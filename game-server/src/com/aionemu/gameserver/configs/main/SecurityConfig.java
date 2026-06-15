package com.aionemu.gameserver.configs.main;

import java.util.Set;

import com.aionemu.commons.configuration.Property;

public class SecurityConfig {

	@Property(key = "gameserver.security.aion.bin.check", defaultValue = "false")
	public static boolean AION_BIN_CHECK;

	@Property(key = "gameserver.security.antihack.teleportation", defaultValue = "false")
	public static boolean TELEPORTATION;

	@Property(key = "gameserver.security.antihack.speedhack", defaultValue = "false")
	public static boolean SPEEDHACK;

	@Property(key = "gameserver.security.antihack.speedhack.counter", defaultValue = "1")
	public static int SPEEDHACK_COUNTER;

	@Property(key = "gameserver.security.antihack.abnormal", defaultValue = "false")
	public static boolean ABNORMAL;

	@Property(key = "gameserver.security.antihack.abnormal.counter", defaultValue = "1")
	public static int ABNORMAL_COUNTER;

	@Property(key = "gameserver.security.antihack.punish", defaultValue = "0")
	public static int PUNISH;

	/**
	 * Check for no-animation hacks (prevents premature skill executions and logs suspicious players to audit log)
	 */
	@Property(key = "gameserver.security.check_animations", defaultValue = "true")
	public static boolean CHECK_ANIMATIONS;

	@Property(key = "gameserver.security.captcha.enable", defaultValue = "false")
	public static boolean CAPTCHA_ENABLE;

	@Property(key = "gameserver.security.captcha.appear", defaultValue = "OD")
	public static String CAPTCHA_APPEAR;

	@Property(key = "gameserver.security.captcha.appear.rate", defaultValue = "5")
	public static int CAPTCHA_APPEAR_RATE;

	@Property(key = "gameserver.security.captcha.extraction.ban.time", defaultValue = "3000")
	public static int CAPTCHA_EXTRACTION_BAN_TIME;

	@Property(key = "gameserver.security.captcha.extraction.ban.add.time", defaultValue = "600")
	public static int CAPTCHA_EXTRACTION_BAN_ADD_TIME;

	@Property(key = "gameserver.security.captcha.bonus.fp.time", defaultValue = "5")
	public static int CAPTCHA_BONUS_FP_TIME;

	@Property(key = "gameserver.security.passkey.enable", defaultValue = "false")
	public static boolean PASSKEY_ENABLE;

	@Property(key = "gameserver.security.passkey.wrong.maxcount", defaultValue = "5")
	public static int PASSKEY_WRONG_MAXCOUNT;

	@Property(key = "gameserver.security.pingcheck.kick", defaultValue = "true")
	public static boolean PINGCHECK_KICK;

	@Property(key = "gameserver.security.flood.delay", defaultValue = "1")
	public static int FLOOD_DELAY;

	@Property(key = "gameserver.security.flood.msg", defaultValue = "6")
	public static int FLOOD_MSG;

	@Property(key = "gameserver.security.validation.flypath", defaultValue = "false")
	public static boolean ENABLE_FLYPATH_VALIDATOR;

	@Property(key = "gameserver.security.survey.delay.minute", defaultValue = "20")
	public static int SURVEY_DELAY;

	/**
	 * Restriction mode for multi-clienting:<br>
	 * NONE - Players are allowed to log in multiple accounts per computer<br>
	 * FULL - Players are allowed to log in one account per computer<br>
	 * SAME_FACTION - Players are allowed to log in multiple accounts per computer, but only log in characters of the same faction<br>
	 */
	@Property(key = "gameserver.security.multi_clienting.restriction_mode", defaultValue = "NONE")
	public static MultiClientingRestrictionMode MULTI_CLIENTING_RESTRICTION_MODE;

	/**
	 * Comma separated list of MAC addresses that are allowed to log in regardless of the configured restrictions.
	 */
	@Property(key = "gameserver.security.multi_clienting.ignored_mac_addresses", defaultValue = "")
	public static Set<String> MULTI_CLIENTING_IGNORED_MAC_ADDRESSES;

	/**
	 * If multi-clienting is restricted to the same faction, logging in characters of one faction will be denied until all characters of the opposite
	 * faction have been offline for the specified amount of time.
	 */
	@Property(key = "gameserver.security.multi_clienting.faction_switch_cooldown_minutes", defaultValue = "20")
	public static int MULTI_CLIENTING_FACTION_SWITCH_COOLDOWN_MINUTES;

	@Property(key = "gameserver.security.hdd_serial_lock.enable", defaultValue = "false")
	public static boolean HDD_SERIAL_LOCK_ENABLE;

	@Property(key = "gameserver.security.hdd_serial_lock.auto_lock", defaultValue = "false")
	public static boolean HDD_SERIAL_LOCK_UNLOCKED_ACCOUNTS;

	public enum MultiClientingRestrictionMode {
		NONE, FULL, SAME_FACTION
	}
}
