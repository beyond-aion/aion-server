package com.aionemu.loginserver.network.factories;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.network.aion.AionClientPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;
import com.aionemu.loginserver.network.aion.LoginConnection.State;
import com.aionemu.loginserver.network.aion.clientpackets.*;

/**
 * @author -Nemesiss-
 */
public class AionPacketHandlerFactory {

	private static final Logger log = LoggerFactory.getLogger(AionPacketHandlerFactory.class);

	/**
	 * Reads one packet from given ByteBuffer
	 * 
	 * @param data
	 * @param client
	 * @return AionClientPacket object from binary data
	 */
	public static AionClientPacket handle(ByteBuffer data, LoginConnection client) {
		/*
			retail (KOR 8.2.22) client packet names for opcodes:
			0 AQ_LOGIN
			1 AQ_SERVER_LIST
			2 AQ_ABOUT_TO_PLAY
			3 AQ_LOGOUT
			4 AQ_LOGIN_MD5
			5 AQ_SERVER_LIST_EX
			6 AQ_SCCHECK
			7 AQ_GAMEGUARD
			8 AQ_UPDATE_SESSION_REQ
			9 AQ_WEBSESSION_LOGIN
			10 AQ_OTPCHECK
			11 AQ_EXTERNAL_TOKEN_LOGIN
			12 AQ_AUX_AUTHENTICATION_ACK
			16 AQ_IOVATION_CHECK
			18 AQ_LOGIN_TOKEN
			---------------------------
			retail (KOR 8.2.22) server packet names for opcodes:
			0 AC_PROTOCOL_VER
			1 AC_LOGIN_FAIL
			2 AC_BLOCKED_ACCOUNT
			3 AC_LOGIN_OK
			4 AC_SEND_SERVER_LIST
			5 AC_SEND_SERVER_FAIL
			6 AC_PLAY_FAIL
			7 AC_PLAY_OK
			8 AC_ACCOUNT_KICKED
			9 AC_BLOCKED_ACCOUNT_WITH_MSG
			10 AC_SCCHECK_REQ
			11 AC_GAMEGUARD
			12 AC_UPDATE_SESSION_ACK
			13 AC_OTPCHECK_REQ
			14 AC_AUX_AUTHENTICATION_REQ
			15 AC_TELEPHONEAUTH_STARTED
			19 AC_IOVATION_CHECK_REQ
		 */
		AionClientPacket msg = null;
		State state = client.getState();
		int opCode = data.get() & 0xFF;

		switch (state) {
			case CONNECTED:
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
			case AUTHED_GG:
				switch (opCode) {
					case 0x00:
						msg = new CM_LOGIN(data, client, opCode);
						break;
					default:
						unknownPacket(opCode, state, data);
				}
				break;
			case AUTHED_LOGIN:
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
