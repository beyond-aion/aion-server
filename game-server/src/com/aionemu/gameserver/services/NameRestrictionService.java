package com.aionemu.gameserver.services;

import com.aionemu.gameserver.configs.main.LegionConfig;
import com.aionemu.gameserver.configs.main.NameConfig;

/**
 * @author nrg
 */
public class NameRestrictionService {

	private static final String ENCODED_BAD_WORD = "----";
	private static String[] forbiddenSequences;
	private static String[] forbiddenByClient;

	public static boolean isValidName(String name) {
		return NameConfig.CHAR_NAME_PATTERN.matcher(name).matches();
	}

	public static boolean isValidPetName(String name) {
		return NameConfig.PET_NAME_PATTERN.matcher(name).matches();
	}

	public static boolean isValidLegionName(String name) {
		return LegionConfig.LEGION_NAME_PATTERN.matcher(name).matches();
	}

	/**
	 * Checks if a name is forbidden
	 * 
	 * @param name
	 * @return true if name is forbidden
	 */
	public static boolean isForbiddenWord(String name) {
		return isForbiddenByClient(name) || isForbiddenBySequence(name);
	}

	/**
	 * Checks if a name is forbidden (contains string sequences from config)
	 * 
	 * @param name
	 * @return True if name is forbidden.
	 */
	private static boolean isForbiddenByClient(String name) {
		if (NameConfig.NAME_FORBIDDEN_CLIENT.equals(""))
			return false;

		if (forbiddenByClient == null || forbiddenByClient.length == 0)
			forbiddenByClient = NameConfig.NAME_FORBIDDEN_CLIENT.split(",");

		for (String s : forbiddenByClient) {
			if (name.equalsIgnoreCase(s))
				return true;
		}
		return false;
	}

	/**
	 * Checks if a part of a string contains a forbidden sequence.
	 * 
	 * @param string
	 * @return True if string contains forbidden sequence.
	 */
	private static boolean isForbiddenBySequence(String string) {
		if (NameConfig.NAME_SEQUENCE_FORBIDDEN.equals(""))
			return false;

		if (forbiddenSequences == null || forbiddenSequences.length == 0)
			forbiddenSequences = NameConfig.NAME_SEQUENCE_FORBIDDEN.toLowerCase().split(",");

		for (String s : forbiddenSequences) {
			if (string.toLowerCase().contains(s))
				return true;
		}
		return false;
	}

	/**
	 * Filters chatmessages
	 * 
	 * @param message
	 * @return
	 */
	public static String filterMessage(String message) {
		for (String word : message.split(" ")) {
			if (isForbiddenWord(word))
				message.replace(word, ENCODED_BAD_WORD);
		}
		return message;
	}
}
