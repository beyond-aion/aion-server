package com.aionemu.gameserver.network.chatserver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.aionemu.commons.utils.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.network.chatserver.ChatServerConnection.State;
import com.aionemu.gameserver.network.chatserver.clientpackets.CM_CS_AUTH_RESPONSE;
import com.aionemu.gameserver.network.chatserver.clientpackets.CM_CS_PLAYER_AUTH_RESPONSE;

/**
 * @author Neon
 */
public class CsClientPacketFactory {

	private static final Logger log = LoggerFactory.getLogger(CsClientPacketFactory.class);
	private static final Map<Integer, PacketInfo<? extends CsClientPacket>> packets = new HashMap<>();

	static {
		try {
			packets.put(0x00, new PacketInfo<>(CM_CS_AUTH_RESPONSE.class, State.CONNECTED));
			packets.put(0x01, new PacketInfo<>(CM_CS_PLAYER_AUTH_RESPONSE.class, State.AUTHED));
		} catch (NoSuchMethodException e) { // should never happen
			throw new ExceptionInInitializerError(e);
		}
	}

	public static CsClientPacket tryCreatePacket(ByteBuffer data, ChatServerConnection client) {
		State state = client.getState();
		int opCode = data.get() & 0xff;
		PacketInfo<? extends CsClientPacket> packetInfo = packets.get(opCode);
		if (packetInfo == null) {
			log.warn(String.format(client + " sent data with unknown opcode: 0x%02X, state=%s %n%s", opCode, state.toString(), NetworkUtils.toHex(data)));
			return null;
		}
		if (!packetInfo.isValid(state)) {
			log.warn(client + " sent " + packetInfo.getPacketClassName() + " but the connections current state (" + state
				+ ") is invalid for this packet. Packet won't be instantiated.");
			return null;
		}
		return packetInfo.newPacket(opCode, data, client);
	}

	private static class PacketInfo<T extends CsClientPacket> {

		private final Constructor<T> packetConstructor;
		private final Set<State> validStates;

		private PacketInfo(Class<T> packetClass, State state, State... otherStates) throws NoSuchMethodException {
			this.packetConstructor = packetClass.getConstructor(int.class);
			this.validStates = EnumSet.of(state, otherStates);
		}

		private boolean isValid(State state) {
			return validStates.contains(state);
		}

		private T newPacket(int opCode, ByteBuffer buffer, ChatServerConnection con) {
			try {
				T packet = packetConstructor.newInstance(opCode);
				packet.setBuffer(buffer);
				packet.setConnection(con);
				return packet;
			} catch (InstantiationException | IllegalAccessException | InvocationTargetException e) { // should never happen
				throw new InstantiationError("Couldn't instantiate packet " + getPacketClassName() + ": " + e);
			}
		}

		private String getPacketClassName() {
			return packetConstructor.getDeclaringClass().getSimpleName();
		}
	}
}
