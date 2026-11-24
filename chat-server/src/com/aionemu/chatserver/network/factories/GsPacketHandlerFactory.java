package com.aionemu.chatserver.network.factories;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.network.gameserver.GsClientPacket;
import com.aionemu.chatserver.network.gameserver.GsConnection;
import com.aionemu.chatserver.network.gameserver.GsConnection.GameServerConnectionState;
import com.aionemu.chatserver.network.gameserver.clientpackets.CM_CS_AUTH;
import com.aionemu.chatserver.network.gameserver.clientpackets.CM_PLAYER_AUTH;
import com.aionemu.chatserver.network.gameserver.clientpackets.CM_PLAYER_GAG;
import com.aionemu.chatserver.network.gameserver.clientpackets.CM_PLAYER_LOGOUT;

import io.netty.buffer.ByteBuf;

/**
 * @author -Nemesiss-
 */
public class GsPacketHandlerFactory {

	private static final Logger log = LoggerFactory.getLogger(GsPacketHandlerFactory.class);

	public static GsClientPacket handle(ByteBuf data, GsConnection client) {
		GameServerConnectionState state = client.getState();
		int opCode = data.readUnsignedByte();
		GsClientPacket packet = switch (state) {
			case CONNECTED -> switch (opCode) {
				case 0x00 -> new CM_CS_AUTH(data, client, opCode);
				default -> null;
			};
			case AUTHED -> switch (opCode) {
				case 0x01 -> new CM_PLAYER_AUTH(data, client, opCode);
				case 0x02 -> new CM_PLAYER_LOGOUT(data, client, opCode);
				case 0x03 -> new CM_PLAYER_GAG(data, client, opCode);
				default -> null;
			};
		};
		if (packet == null)
			log.warn("Unknown packet received from Game Server: {} state {}", "0x%02X".formatted(opCode), state);
		return packet;
	}
}
