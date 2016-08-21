package com.aionemu.gameserver.network.loginserver;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.network.loginserver.LoginServerConnection.State;

/**
 * @author -Nemesiss-
 * @author Luno
 */
public class LsPacketHandler {

	/**
	 * logger for this class
	 */
	private static final Logger log = LoggerFactory.getLogger(LsPacketHandler.class);

	private static Map<State, Map<Integer, LsClientPacket>> packetPrototypes = new HashMap<>();

	/**
	 * Reads one packet from given ByteBuffer
	 * 
	 * @param data
	 * @param client
	 * @return GsClientPacket object from binary data
	 */
	public LsClientPacket handle(ByteBuffer data, LoginServerConnection client) {
		State state = client.getState();
		int id = data.get() & 0xff;

		return getPacket(state, id, data, client);
	}

	public void addPacketPrototype(LsClientPacket packetPrototype, State... states) {
		for (State state : states) {
			Map<Integer, LsClientPacket> pm = packetPrototypes.get(state);
			if (pm == null) {
				pm = new HashMap<>();
				packetPrototypes.put(state, pm);
			}
			pm.put(packetPrototype.getOpcode(), packetPrototype);
		}
	}

	private LsClientPacket getPacket(State state, int id, ByteBuffer buf, LoginServerConnection con) {
		LsClientPacket prototype = null;

		Map<Integer, LsClientPacket> pm = packetPrototypes.get(state);
		if (pm != null) {
			prototype = pm.get(id);
		}

		if (prototype == null) {
			unknownPacket(state, id);
			return null;
		}

		LsClientPacket res = prototype.clonePacket();
		res.setBuffer(buf);
		res.setConnection(con);

		return res;
	}

	/**
	 * Logs unknown packet.
	 * 
	 * @param state
	 * @param id
	 */
	private void unknownPacket(State state, int id) {
		log.warn(String.format("Unknown packet recived from Login Server: 0x%02X state=%s", id, state.toString()));
	}
}
