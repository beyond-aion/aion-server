package com.aionemu.gameserver.utils;

import java.nio.ByteBuffer;

import com.aionemu.gameserver.configs.main.NameConfig;

/**
 * @author -Nemesiss-
 */
public class Util {

	/**
	 * Convert data from given ByteBuffer to hex
	 * 
	 * @param data
	 * @return hex
	 */
	public static String toHex(ByteBuffer data) {
		StringBuilder result = new StringBuilder();
		int counter = 0;
		int b;
		while (data.hasRemaining()) {
			if (counter % 16 == 0)
				result.append(String.format("%04X: ", counter));

			b = data.get() & 0xff;
			result.append(String.format("%02X ", b));

			counter++;
			if (counter % 16 == 0) {
				result.append("  ");
				toText(data, result, 16);
				result.append("\n");
			}
		}
		int rest = counter % 16;
		if (rest > 0) {
			for (int i = 0; i < 17 - rest; i++) {
				result.append("   ");
			}
			toText(data, result, rest);
		}
		return result.toString();
	}

	/**
	 * Gets last <tt>cnt</tt> read bytes from the <tt>data</tt> buffer and puts into <tt>result</tt> buffer in special
	 * format:
	 * <ul>
	 * <li>if byte represents char from partition 0x1F to 0x80 (which are normal ascii chars) then it's put into buffer as
	 * it is</li>
	 * <li>otherwise dot is put into buffer</li>
	 * </ul>
	 * 
	 * @param data
	 * @param result
	 * @param cnt
	 */
	private static void toText(ByteBuffer data, StringBuilder result, int cnt) {
		int charPos = data.position() - cnt;
		for (int a = 0; a < cnt; a++) {
			int c = data.get(charPos++);
			if (c > 0x1f && c < 0x80)
				result.append((char) c);
			else
				result.append('.');
		}
	}

	/**
	 * Converts name to valid pattern For example : "atracer" -> "Atracer"
	 * 
	 * @param name
	 * @return String
	 */
	public static String convertName(String name) {
		if (!name.isEmpty()) {
			if(NameConfig.ALLOW_CUSTOM_NAMES)
				return name;
			else
				return name.substring(0, 1).toUpperCase() + name.toLowerCase().substring(1);
		}
		else
			return "";
	}
}
