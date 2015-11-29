package com.aionemu.gameserver.services;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.configs.main.LegionConfig;
import com.aionemu.gameserver.configs.main.NameConfig;

/**
 * @author nrg
 * @modified Neon
 */
public class NameRestrictionService {

	private static String[] forbiddenSequences = NameConfig.NAME_SEQUENCE_FORBIDDEN.toLowerCase().replaceAll(",+ +,+|,,+", ",").split(",");
	private static String[] forbiddenByClient = NameConfig.NAME_FORBIDDEN_CLIENT.replaceAll(",+ +,+|,,+", ",").split(",");

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
	 * @param string
	 * @return true if name is forbidden
	 */
	public static boolean isForbidden(String string) {
		return isForbiddenByClient(string) || containsForbiddenSequence(string);
	}

	/**
	 * Checks if a name is forbidden (contains string sequences from config)
	 * 
	 * @param string
	 * @return True if name is forbidden.
	 */
	private static boolean isForbiddenByClient(String string) {
		if (forbiddenByClient[0].isEmpty())
			return false;

		for (String s : forbiddenByClient) {
			if (string.equalsIgnoreCase(s))
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
	private static boolean containsForbiddenSequence(String string) {
		if (forbiddenSequences[0].isEmpty())
			return false;

		string = string.toLowerCase();
		for (String s : forbiddenSequences) {
			if (string.contains(s))
				return true;
		}
		return false;
	}

	/**
	 * Filters chat messages.
	 * 
	 * @param message
	 * @return
	 */
	public static String filterMessage(String message) {
		for (String word : message.split(" ")) {
			if (isForbidden(word))
				message = message.replace(word, StringUtils.repeat("*", word.length()));
		}
		return message;
	}
}
