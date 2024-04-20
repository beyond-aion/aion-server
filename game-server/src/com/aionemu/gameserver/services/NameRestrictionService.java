package com.aionemu.gameserver.services;

import org.apache.commons.lang3.StringUtils;

import com.aionemu.gameserver.configs.main.LegionConfig;
import com.aionemu.gameserver.configs.main.NameConfig;

/**
 * @author nrg, Neon
 */
public class NameRestrictionService {

	public static boolean isValidName(String name) {
		return NameConfig.CHAR_NAME_PATTERN.matcher(name).matches();
	}

	public static boolean isValidPetName(String name) {
		return NameConfig.PET_NAME_PATTERN.matcher(name).matches();
	}

	public static boolean isValidLegionName(String name) {
		return LegionConfig.LEGION_NAME_PATTERN.matcher(name).matches();
	}

	public static boolean isForbidden(String name) {
		return containsForbiddenSequence(name) || isForbiddenWord(name);
	}

	private static boolean containsForbiddenSequence(String name) {
		if (NameConfig.FORBIDDEN_SEQUENCE_PATTERN == null)
			return false;

		return NameConfig.FORBIDDEN_SEQUENCE_PATTERN.matcher(name).find();
	}

	public static boolean isForbiddenWord(String string) {
		for (String s : NameConfig.FORBIDDEN_WORDS) {
			if (string.equalsIgnoreCase(s))
				return true;
		}
		return false;
	}

	/**
	 * @return The filtered chat message (forbidden words are replaced by *'s)
	 */
	public static String filterMessage(String message) {
		for (String word : message.split(" ")) {
			if (isForbiddenWord(word))
				message = message.replace(word, StringUtils.repeat("*", word.length()));
		}
		return message;
	}
}
