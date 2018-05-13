package com.aionemu.gameserver.network.loginserver;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.NetworkUtils;
import com.aionemu.gameserver.network.loginserver.LoginServerConnection.State;
import com.aionemu.gameserver.network.loginserver.clientpackets.*;

/**
 * @author Neon
 */
public class LsClientPacketFactory {

	private static final Logger log = LoggerFactory.getLogger(LsClientPacketFactory.class);
	private static final Map<Integer, PacketInfo<? extends LsClientPacket>> packets = new HashMap<>();

	static {
		try {
			packets.put(0x00, new PacketInfo<>(CM_GS_AUTH_RESPONSE.class, State.CONNECTED));
			packets.put(0x01, new PacketInfo<>(CM_ACOUNT_AUTH_RESPONSE.class, State.AUTHED));
			packets.put(0x02, new PacketInfo<>(CM_REQUEST_KICK_ACCOUNT.class, State.AUTHED));
			packets.put(0x03, new PacketInfo<>(CM_ACCOUNT_RECONNECT_KEY.class, State.AUTHED));
			packets.put(0x04, new PacketInfo<>(CM_LS_CONTROL_RESPONSE.class, State.AUTHED));
			packets.put(0x05, new PacketInfo<>(CM_BAN_RESPONSE.class, State.AUTHED));
			packets.put(0x08, new PacketInfo<>(CM_GS_CHARACTER_RESPONSE.class, State.AUTHED));
			packets.put(0x09, new PacketInfo<>(CM_MACBAN_LIST.class, State.AUTHED));
			packets.put(0x0A, new PacketInfo<>(CM_PREMIUM_RESPONSE.class, State.AUTHED));
			packets.put(0x0B, new PacketInfo<>(CM_LS_PING.class, State.AUTHED));
			packets.put(0x0C, new PacketInfo<>(CM_PTRANSFER_RESPONSE.class, State.AUTHED));
			packets.put(0x0D, new PacketInfo<>(CM_HDD_BANLIST.class, State.AUTHED));
		} catch (NoSuchMethodException e) { // should never happen
			throw new ExceptionInInitializerError(e);
		}
	}

	public static LsClientPacket tryCreatePacket(ByteBuffer data, LoginServerConnection client) {
		State state = client.getState();
		int opCode = data.get() & 0xff;
		PacketInfo<? extends LsClientPacket> packetInfo = packets.get(opCode);
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

	private static class PacketInfo<T extends LsClientPacket> {

		private final Constructor<T> packetConstructor;
		private final Set<State> validStates;

		private PacketInfo(Class<T> packetClass, State state, State... otherStates) throws NoSuchMethodException {
			this.packetConstructor = packetClass.getConstructor(int.class);
			this.validStates = EnumSet.of(state, otherStates);
		}

		private boolean isValid(State state) {
			return validStates.contains(state);
		}

		private T newPacket(int opCode, ByteBuffer buffer, LoginServerConnection con) {
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
