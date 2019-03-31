package com.aionemu.gameserver.configs.network;

import java.util.Map;

import com.aionemu.commons.configuration.Properties;
import com.aionemu.commons.configuration.Property;
import com.aionemu.gameserver.network.aion.AionClientPacket;

/**
 * @author Neon
 */
public class PffConfig {

	@Property(key = "gameserver.network.pff.mode", defaultValue = "1")
	public static int PFF_MODE;

	@Properties(keyPattern = "^gameserver\\.network\\.pff\\.packet\\.(0[xX][0-9a-fA-F]+)$")
	public static Map<Integer, Integer> THRESHOLD_MILLIS_BY_PACKET_OPCODE;

	/**
	 * @return The allowed delay in milliseconds in which two packets of the given type may be sent from one client.
	 */
	public static int getAllowedMillisBetweenPackets(AionClientPacket packet) {
		return THRESHOLD_MILLIS_BY_PACKET_OPCODE.getOrDefault(packet.getOpCode(), 0);
	}
}
