package com.aionemu.gameserver.utils;

import com.aionemu.gameserver.configs.main.NameConfig;

/**
 * @author -Nemesiss-
 */
public class Util {

	/**
	 * Converts name to valid pattern For example : "atracer" -> "Atracer"
	 * 
	 * @param name
	 * @return String
	 */
	public static String convertName(String name) {
		if (!name.isEmpty()) {
			if (NameConfig.ALLOW_CUSTOM_NAMES)
				return name;
			else
				return name.substring(0, 1).toUpperCase() + name.toLowerCase().substring(1);
		} else
			return "";
	}
}
