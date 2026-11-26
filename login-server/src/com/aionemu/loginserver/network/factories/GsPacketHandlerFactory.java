package com.aionemu.loginserver.network.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsConnection.State;
import com.aionemu.loginserver.network.gameserver.clientpackets.*;

import io.netty.buffer.ByteBuf;

/**
 * @author -Nemesiss-
 */
public class GsPacketHandlerFactory {

	private static final Logger log = LoggerFactory.getLogger(GsPacketHandlerFactory.class);

	/**
	 * Reads one packet from given buffer
	 */
	public static GsClientPacket handle(ByteBuf data, GsConnection client) {
		State state = client.getState();
		int opcode = data.readUnsignedByte();
		GsClientPacket packet = switch (state) {
			case CONNECTED -> switch (opcode) {
				case 0 -> new CM_GS_AUTH(data, client, opcode);
				default -> null;
			};
			case AUTHED -> switch (opcode) {
				case 1 -> new CM_ACCOUNT_AUTH(data, client, opcode);
				case 2 -> new CM_ACCOUNT_RECONNECT_KEY(data, client, opcode);
				case 3 -> new CM_ACCOUNT_DISCONNECTED(data, client, opcode);
				case 4 -> new CM_ACCOUNT_LIST(data, client, opcode);
				case 5 -> new CM_LS_CONTROL(data, client, opcode);
				case 6 -> new CM_BAN(data, client, opcode);
				case 7 -> new CM_ACCOUNT_CONNECTION_INFO(data, client, opcode);
				case 8 -> new CM_GS_CHARACTER(data, client, opcode);
				case 9 -> new CM_MACBAN_CONTROL(data, client, opcode);
				case 10 -> new CM_HDDBAN_CONTROL(data, client, opcode);
				case 11 -> new CM_CHANGE_ALLOWED_HDD_SERIAL(data, client, opcode);
				case 12 -> new CM_GS_PONG(data, client, opcode);
				case 13 -> new CM_PTRANSFER_CONTROL(data, client, opcode);
				default -> null;
			};
		};
		if (packet == null)
			log.warn("Unknown packet received from Game Server: {} state {}", "0x%02X".formatted(opcode), state);
		return packet;
	}
}
