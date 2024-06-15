package com.aionemu.gameserver.configs.main;

import java.util.Set;

import com.aionemu.commons.configuration.Property;
import com.aionemu.gameserver.network.aion.AionClientPacket;

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

	@Property(key = "gameserver.security.noanimation", defaultValue = "false")
	public static boolean NO_ANIMATION;

	@Property(key = "gameserver.security.noanimation.kick", defaultValue = "false")
	public static boolean NO_ANIMATION_KICK;

	@Property(key = "gameserver.security.noanimation.value", defaultValue = "0.1")
	public static float NO_ANIMATION_VALUE;

	@Property(key = "gameserver.security.motion.time.enable", defaultValue = "true")
	public static boolean MOTION_TIME;

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

	@Property(key = "gameserver.security.check.dualboxing", defaultValue = "false")
	public static boolean DUALBOXING;

	@Property(key = "gameserver.security.kick.dualboxing", defaultValue = "false")
	public static boolean KICK_DUALBOXING;

	@Property(key = "gameserver.security.hdd_serial_lock.enable", defaultValue = "false")
	public static boolean HDD_SERIAL_LOCK_ENABLE;

	@Property(key = "gameserver.security.hdd_serial_lock.auto_lock", defaultValue = "false")
	public static boolean HDD_SERIAL_LOCK_UNLOCKED_ACCOUNTS;
}
