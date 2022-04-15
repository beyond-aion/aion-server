package com.aionemu.commons.utils;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.ByteBuffer;

/**
 * @author KID, -Nemesiss-
 */
public class NetworkUtils {

	/**
	 * @return The first matching non-loopback IPv4 address on this machine (meaning network reachable, so ignoring IPs like 127.0.0.1)
	 */
	public static InetAddress findLocalIPv4() {
		try {
			return NetworkInterface.networkInterfaces()
														 .flatMap(NetworkInterface::inetAddresses)
														 .filter(inetAddress -> inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress() && !inetAddress.isMulticastAddress())
														 .findFirst()
														 .orElse(null);
		} catch (SocketException ignored) {
			return null;
		}
	}

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
	 * @return Formatted hex string of the buffers data.
	 */
	public static String toHex(ByteBuffer buffer) {
		return toHex(buffer, 0, Math.min(buffer.limit(), buffer.capacity()));
	}

	/**
	 * @param buffer
	 * @param start position to start read from
	 * @param end end position (exclusive)
	 * @return Formatted hex string of the buffers data.
	 */
	public static String toHex(ByteBuffer buffer, int start, int end) {
		StringBuilder result = new StringBuilder();
		for (int i = start, bytes = 0; i < end; bytes++) {
			if (bytes % 16 == 0) {
				if (result.length() > 0)
					result.append("\n");
				result.append(String.format("%04X: ", bytes));
			}

			int b = buffer.get(i) & 0xff;
			result.append(String.format("%02X ", b));

			int bytesInRow = (bytes % 16) + 1;
			if (++i == buffer.capacity() || bytesInRow == 16) {
				for (int j = bytesInRow; j <= 16; j++)
					result.append("   ");
				toText(buffer, result, i - bytesInRow, i);
			}
		}
		return result.toString();
	}

	/**
	 * Writes bytes from the <tt>buffer</tt>'s startIndex (inclusive) to the endIndex (exclusive) as string representable characters into <tt>result</tt>:
	 * <ul>
	 * <li>if byte represents char from partition 0x1F to 0x80 (which are normal ascii chars) then it's put into buffer as it is</li>
	 * <li>otherwise dot is put into buffer</li>
	 * </ul>
	 *
	 * @param buffer
	 * @param result
	 * @param startIndex
	 * @param endIndex exclusive
	 */
	private static void toText(ByteBuffer buffer, StringBuilder result, int startIndex, int endIndex) {
		for (int charPos = startIndex; charPos < endIndex; charPos++) {
			int c = buffer.get(charPos) & 0xFF; // unsigned byte
			if (c > 0x1f && c < 0x80)
				result.append((char) c);
			else
				result.append('.');
		}
	}
}
