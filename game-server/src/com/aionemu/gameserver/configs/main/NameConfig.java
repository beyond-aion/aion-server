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
	@Property(key = "gameserver.name.character_pattern", defaultValue = "[a-zA-Z]{2,16}")
	public static Pattern CHAR_NAME_PATTERN;

	/**
	 * Pet name pattern (checked when pet is being adopted or renamed)
	 */
	@Property(key = "gameserver.name.pet_pattern", defaultValue = "[a-zA-Z]{2,16}")
	public static Pattern PET_NAME_PATTERN;

	/**
	 * Forbidden word sequences (Regex pattern).<br>
	 * Filters names.
	 */
	@Property(key = "gameserver.name.forbidden_sequences_pattern")
	public static Pattern FORBIDDEN_SEQUENCE_PATTERN;

	/**
	 * Forbidden words.<br>
	 * Filters names & chat.
	 */
	@Property(key = "gameserver.name.forbidden_words")
	public static String[] FORBIDDEN_WORDS;

	/**
	 * Number of days a name is reserved after renaming. During this time, only the renamed player can (re)adopt this name.
	 */
	@Property(key = "gameserver.name.reserve_old_name_days", defaultValue = "30")
	public static int RESERVE_OLD_NAME_DAYS;
}
