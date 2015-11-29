package com.aionemu.gameserver.configs.main;

import java.util.regex.Pattern;

import com.aionemu.commons.configuration.Property;

/**
 * @author nrg
 */
public class NameConfig {

	/**
	 * Enables custom names usage.
	 */
	@Property(key = "gameserver.name.allow.custom", defaultValue = "false")
	public static boolean ALLOW_CUSTOM_NAMES;

	/**
	 * Character name pattern (checked when character is being created or renamed)
	 */
	@Property(key = "gameserver.name.characterpattern", defaultValue = "[a-zA-Z]{2,16}")
	public static Pattern CHAR_NAME_PATTERN;

	/**
	 * Pet name pattern (checked when pet is being adopted or renamed)
	 */
	@Property(key = "gameserver.name.pet_pattern", defaultValue = "[a-zA-Z]{2,16}")
	public static Pattern PET_NAME_PATTERN;

	/**
	 * Forbidden word sequences.<br>
	 * Filters charname, miol, legion, chat
	 */
	@Property(key = "gameserver.name.forbidden.sequences", defaultValue = "")
	public static String NAME_SEQUENCE_FORBIDDEN;

	/**
	 * Forbidden names.<br>
	 * Filters charname, miol, legion, chat
	 */
	@Property(key = "gameserver.name.forbidden.client", defaultValue = "")
	public static String NAME_FORBIDDEN_CLIENT;
}
