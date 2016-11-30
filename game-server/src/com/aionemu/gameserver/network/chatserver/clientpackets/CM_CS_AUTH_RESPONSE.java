package com.aionemu.gameserver.network.chatserver.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.network.chatserver.ChatServer;
import com.aionemu.gameserver.network.chatserver.ChatServerConnection.State;
import com.aionemu.gameserver.network.chatserver.CsClientPacket;

/**
 * @author ATracer, Neon
 */
public class CM_CS_AUTH_RESPONSE extends CsClientPacket {

	protected static final Logger log = LoggerFactory.getLogger(CM_CS_AUTH_RESPONSE.class);
	private byte response;
	private byte[] ip;
	private int port;

	/**
	 * @param opcode
	 */
	public CM_CS_AUTH_RESPONSE(int opcode) {
		super(opcode);
	}

	@Override
	protected void readImpl() {
		response = readC();
		if (response == 0) {
			ip = readB(readUC());
			port = readUH();
		}
	}

	@Override
	protected void runImpl() {
		switch (response) {
			case 0: // Authed
				getConnection().setState(State.AUTHED);
				ChatServer.getInstance().setPublicAddress(ip, port);
				break;
			case 1: // Not authed
				log.warn("GameServer is not authenticated at ChatServer side!");
				getConnection().close();
				break;
			case 2: // Already registered
				log.warn("GameServer is already registered at ChatServer side!");
				getConnection().close();
				break;
		}
	}
}
