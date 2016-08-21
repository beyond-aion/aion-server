package com.aionemu.gameserver.network.aion;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;

/**
 * @author -Nemesiss-
 * @author Luno
 */
public class AionPacketHandler {

	/**
	 * logger for this class
	 */
	private static final Logger log = LoggerFactory.getLogger(AionPacketHandler.class);

	private Map<Integer, AionClientPacket> packetsPrototypes = new HashMap<>();

	/**
	 * Reads one packet from given ByteBuffer
	 * 
	 * @param data
	 * @param client
	 * @return AionClientPacket object from binary data
	 */
	public AionClientPacket handle(ByteBuffer data, AionConnection client) {
		State state = client.getState();
		int id = data.getShort() & 0xffff;
		/* Second opcodec. */
		data.position(data.position() + 3);

		return getPacket(state, id, data, client);
	}

	public void addPacketPrototype(AionClientPacket packetPrototype) {
		packetsPrototypes.put(packetPrototype.getOpcode(), packetPrototype);
	}

	private AionClientPacket getPacket(State state, int id, ByteBuffer buf, AionConnection con) {
		AionClientPacket prototype = packetsPrototypes.get(id);

		if (prototype == null) {
			unknownPacket(state, id, buf);
			return null;
		}

		AionClientPacket res = prototype.clonePacket();
		res.setBuffer(buf);
		res.setConnection(con);

		if (con.getState().equals(State.IN_GAME) && con.getActivePlayer().getPlayerAccount().getMembership() == 10) {
			PacketSendUtility.sendMessage(con.getActivePlayer(), "0x" + Integer.toHexString(res.getOpcode()).toUpperCase() + " : " + res.getPacketName());
		}
		return res;
	}

	/**
	 * Logs unknown packet.
	 * 
	 * @param state
	 * @param id
	 * @param data
	 */
	private void unknownPacket(State state, int id, ByteBuffer data) {
		if (NetworkConfig.DISPLAY_UNKNOWNPACKETS) {
			log.warn(String.format("Unknown packet recived from Aion client: 0x%04X, state=%s %n%s", id, state.toString(), Util.toHex(data)));
		}
	}
}
