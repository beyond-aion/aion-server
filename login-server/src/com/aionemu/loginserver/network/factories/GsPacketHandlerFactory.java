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
	 * 
	 * @param data
	 * @param client
	 * @return GsClientPacket object from binary data
	 */
	public static GsClientPacket handle(ByteBuf data, GsConnection client) {
		GsClientPacket msg = null;
		State state = client.getState();
		int id = data.readUnsignedByte();

		switch (state) {
			case CONNECTED:
				msg = switch (id) {
					case 0 -> new CM_GS_AUTH(data, client, id);
					default -> null;
				};
				break;
			case AUTHED:
				msg = switch (id) {
					case 1 -> new CM_ACCOUNT_AUTH(data, client, id);
					case 2 -> new CM_ACCOUNT_RECONNECT_KEY(data, client, id);
					case 3 -> new CM_ACCOUNT_DISCONNECTED(data, client, id);
					case 4 -> new CM_ACCOUNT_LIST(data, client, id);
					case 5 -> new CM_LS_CONTROL(data, client, id);
					case 6 -> new CM_BAN(data, client, id);
					case 7 -> new CM_ACCOUNT_CONNECTION_INFO(data, client, id);
					case 8 -> new CM_GS_CHARACTER(data, client, id);
					case 9 -> new CM_MACBAN_CONTROL(data, client, id);
					case 10 -> new CM_HDDBAN_CONTROL(data, client, id);
					case 11 -> new CM_CHANGE_ALLOWED_HDD_SERIAL(data, client, id);
					case 12 -> new CM_GS_PONG(data, client, id);
					case 13 -> new CM_PTRANSFER_CONTROL(data, client, id);
					default -> null;
				};
				break;
		}

		if (msg == null)
			log.warn(String.format("Unknown packet received from Game Server: 0x%02X state=%s", id, state));

		return msg;
	}
}
