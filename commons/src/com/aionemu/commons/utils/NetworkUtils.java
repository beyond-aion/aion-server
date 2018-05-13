package com.aionemu.commons.utils;

import java.nio.ByteBuffer;

/**
 * @author KID, -Nemesiss-
 */
public class NetworkUtils {

	/**
	 * check if IP address match pattern
	 * 
	 * @param pattern
	 *          *.*.*.* , 192.168.1.0-255 , *
	 * @param address
	 *          - 192.168.1.1<BR>
	 *          <code>address = 10.2.88.12  pattern = *.*.*.*   result: true<BR>
	 *                address = 10.2.88.12  pattern = *   result: true<BR>
	 *                address = 10.2.88.12  pattern = 10.2.88.12-13   result: true<BR>
	 *                address = 10.2.88.12  pattern = 10.2.88.13-125   result: false<BR></code>
	 * @return true if address match pattern
	 */
	public static boolean checkIPMatching(String pattern, String address) {
		if (pattern.equals("*.*.*.*") || pattern.equals("*"))
			return true;

		String[] mask = pattern.split("\\.");
		String[] ip_address = address.split("\\.");
		for (int i = 0; i < mask.length; i++) {
			if (mask[i].equals("*") || mask[i].equals(ip_address[i]))
				continue;
			else if (mask[i].contains("-")) {
				byte min = Byte.parseByte(mask[i].split("-")[0]);
				byte max = Byte.parseByte(mask[i].split("-")[1]);
				byte ip = Byte.parseByte(ip_address[i]);
				if (ip < min || ip > max)
					return false;
			} else
				return false;
		}
		return true;
	}

	/**
	 * @return The IP as a human-readable string (i.e. 127.0.0.1).
	 */
	public static String intToIpString(int ip) {
		return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
	}

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
	 * Gets last <tt>cnt</tt> read bytes from the <tt>data</tt> buffer and puts into <tt>result</tt> buffer in special format:
	 * <ul>
	 * <li>if byte represents char from partition 0x1F to 0x80 (which are normal ascii chars) then it's put into buffer as it is</li>
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
			int c = data.get(charPos++) & 0xFF; // unsigned byte
			if (c > 0x1f && c < 0x80)
				result.append((char) c);
			else
				result.append('.');
		}
	}
}
