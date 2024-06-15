package com.aionemu.gameserver.network.aion.clientpackets;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Set;

import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionConnection.State;

/**
 * In this packet client is sending connection and system information (IPs, MAC Address and HDD serial).
 * 
 * @author -Nemesiss-, KID, ViAl, Neon
 */
public class CM_MAC_ADDRESS extends AionClientPacket {

	private String macAddress, hddSerial;

	public CM_MAC_ADDRESS(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		readC(); // unk
		int routeSteps = readUH();
		for (int i = 0; i < routeSteps; i++)
			readD(); // ip traceroute, see -> System.out.println(NetworkUtils.intToIpString(readD()))
		macAddress = readS();
		hddSerial = fixHddSerial(readS());
		readD(); // local IP, see -> System.out.println(NetworkUtils.intToIpString(readD()))
	}

	@Override
	protected void runImpl() {
		AionConnection con = getConnection();
		con.setMacAddress(macAddress);
		con.setHddSerial(hddSerial);
	}

	private static String fixHddSerial(String hddSerial) {
		if (!hddSerial.isEmpty() && (hddSerial.length() <= 2 || !hddSerial.matches("[0-9a-zA-Z _-]+"))) {
			// not a serial number string (some clients send weird but deterministic data)
			return "0x" + HexFormat.of().formatHex(hddSerial.getBytes(StandardCharsets.UTF_16LE)).toUpperCase();
		} else if (hddSerial.matches("^[a-zA-Z0-9] [a-zA-Z0-9_-].*|.*[a-zA-Z0-9_-] [a-zA-Z0-9]$")) { // check if second or penultimate char is a space
			return hddSerial.replaceAll("(.)(.)", "$2$1").trim(); // for some reason some clients send switched chars
		}
		return hddSerial.trim();
	}
}
