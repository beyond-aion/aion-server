package com.aionemu.loginserver.network.factories;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.network.aion.AionClientPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;
import com.aionemu.loginserver.network.aion.LoginConnection.State;
import com.aionemu.loginserver.network.aion.clientpackets.CM_AUTH_GG;
import com.aionemu.loginserver.network.aion.clientpackets.CM_LOGIN;
import com.aionemu.loginserver.network.aion.clientpackets.CM_PLAY;
import com.aionemu.loginserver.network.aion.clientpackets.CM_SERVER_LIST;
import com.aionemu.loginserver.network.aion.clientpackets.CM_UPDATE_SESSION;

/**
 * @author -Nemesiss-
 */
public class AionPacketHandlerFactory {

	/**
	 * logger for this class
	 */
	private static final Logger log = LoggerFactory.getLogger(AionPacketHandlerFactory.class);

	/**
	 * Reads one packet from given ByteBuffer
	 * 
	 * @param data
	 * @param client
	 * @return AionClientPacket object from binary data
	 */
	public static AionClientPacket handle(ByteBuffer data, LoginConnection client) {
		AionClientPacket msg = null;
		State state = client.getState();
		int opCode = data.get() & 0xFF;

		switch (state) {
			case CONNECTED: {
				switch (opCode) {
					case 0x07:
						msg = new CM_AUTH_GG(data, client, opCode);
						break;
					case 0x08:
						msg = new CM_UPDATE_SESSION(data, client, opCode);
						break;
					default:
						unknownPacket(opCode, state, data);
				}
				break;
			}
			case AUTHED_GG: {
				switch (opCode) {
					case 0x00:
						msg = new CM_LOGIN(data, client, opCode);
						break;
					default:
						unknownPacket(opCode, state, data);
				}
				break;
			}
			case AUTHED_LOGIN: {
				switch (opCode) {
					case 0x05:
						msg = new CM_SERVER_LIST(data, client, opCode);
						break;
					case 0x02:
						msg = new CM_PLAY(data, client, opCode);
						break;
					default:
						unknownPacket(opCode, state, data);
				}
				break;
			}
		}

		return msg;
	}

	/**
	 * Logs an unknown packet
	 */
	private static void unknownPacket(int opCode, State state, ByteBuffer buf) {
		int length = buf.remaining();
		StringBuilder sb = new StringBuilder(length * 3);
		while (buf.hasRemaining())
			sb.append(String.format("%02X ", buf.get()));
		log.warn(String.format("Unknown packet received from client: opCode=0x%02X state=%s length=%d data=[%s]", opCode, state, length, sb.toString().trim()));
	}
}
