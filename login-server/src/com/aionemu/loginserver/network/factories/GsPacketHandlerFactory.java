package com.aionemu.loginserver.network.factories;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsConnection.State;
import com.aionemu.loginserver.network.gameserver.clientpackets.*;

/**
 * @author -Nemesiss-
 */
public class GsPacketHandlerFactory {

	private static final Logger log = LoggerFactory.getLogger(GsPacketHandlerFactory.class);

	/**
	 * Reads one packet from given ByteBuffer
	 * 
	 * @param data
	 * @param client
	 * @return GsClientPacket object from binary data
	 */
	public static GsClientPacket handle(ByteBuffer data, GsConnection client) {
		GsClientPacket msg = null;
		State state = client.getState();
		int id = data.get() & 0xff;

		switch (state) {
			case CONNECTED:
				msg = switch (id) {
					case 0 -> new CM_GS_AUTH();
					default -> null;
				};
				break;
			case AUTHED:
				msg = switch (id) {
					case 1 -> new CM_ACCOUNT_AUTH();
					case 2 -> new CM_ACCOUNT_RECONNECT_KEY();
					case 3 -> new CM_ACCOUNT_DISCONNECTED();
					case 4 -> new CM_ACCOUNT_LIST();
					case 5 -> new CM_LS_CONTROL();
					case 6 -> new CM_BAN();
					case 7 -> new CM_ACCOUNT_CONNECTION_INFO();
					case 8 -> new CM_GS_CHARACTER();
					case 9 -> new CM_MACBAN_CONTROL();
					case 10 -> new CM_HDDBAN_CONTROL();
					case 11 -> new CM_CHANGE_ALLOWED_HDD_SERIAL();
					case 12 -> new CM_GS_PONG();
					case 13 -> new CM_PTRANSFER_CONTROL();
					default -> null;
				};
				break;
		}

		if (msg == null) {
			log.warn(String.format("Unknown packet received from Game Server: 0x%02X state=%s", id, state));
		} else {
			msg.setConnection(client);
			msg.setBuffer(data);
		}

		return msg;
	}
}
