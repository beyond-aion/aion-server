package com.aionemu.chatserver.network.factories;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.network.gameserver.GsClientPacket;
import com.aionemu.chatserver.network.gameserver.GsConnection;
import com.aionemu.chatserver.network.gameserver.GsConnection.GameServerConnectionState;
import com.aionemu.chatserver.network.gameserver.clientpackets.CM_CS_AUTH;
import com.aionemu.chatserver.network.gameserver.clientpackets.CM_PLAYER_AUTH;
import com.aionemu.chatserver.network.gameserver.clientpackets.CM_PLAYER_GAG;
import com.aionemu.chatserver.network.gameserver.clientpackets.CM_PLAYER_LOGOUT;

/**
 * @author -Nemesiss-
 */
public class GsPacketHandlerFactory {

	private static final Logger log = LoggerFactory.getLogger(GsPacketHandlerFactory.class);

	public static GsClientPacket handle(ByteBuffer data, GsConnection client) {
		GsClientPacket msg = null;
		GameServerConnectionState state = client.getState();
		int id = data.get() & 0xff;

		switch (state) {
			case CONNECTED:
				if (id == 0x00)
					msg = new CM_CS_AUTH(data, client);
				else
					logUnknownPacket(id, state);
				break;
			case AUTHED:
				switch (id) {
					case 0x01 -> msg = new CM_PLAYER_AUTH(data, client);
					case 0x02 -> msg = new CM_PLAYER_LOGOUT(data, client);
					case 0x03 -> msg = new CM_PLAYER_GAG(data, client);
					default -> logUnknownPacket(id, state);
				}
				break;
		}

		if (msg != null) {
			msg.setConnection(client);
			msg.setBuffer(data);
		}
		return msg;
	}

	private static void logUnknownPacket(int id, GameServerConnectionState state) {
		log.warn("Unknown packet received from Game Server: {} state {}", "0x%02X".formatted(id), state);
	}
}
