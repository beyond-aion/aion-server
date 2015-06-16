package com.aionemu.gameserver.network.chatserver.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.ExitCode;
import com.aionemu.gameserver.network.chatserver.ChatServerConnection.State;
import com.aionemu.gameserver.network.chatserver.CsClientPacket;
import com.aionemu.gameserver.network.chatserver.serverpackets.SM_CS_AUTH;
import com.aionemu.gameserver.services.ChatService;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public class CM_CS_AUTH_RESPONSE extends CsClientPacket {

	/**
	 * Logger for this class.
	 */
	protected static final Logger log = LoggerFactory.getLogger(CM_CS_AUTH_RESPONSE.class);

	/**
	 * Response: 0=Authed,<br>
	 * 1=NotAuthed,<br>
	 * 2=AlreadyRegistered
	 */
	private int response;
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
		ip = readB(4);
		port = readH();
	}

	@Override
	protected void runImpl() {
		switch (response) {
			case 0: // Authed
				log.info("GameServer authed successfully IP : " + (ip[0] & 0xFF) + "." + (ip[1] & 0xFF) + "." + (ip[2] & 0xFF)
					+ "." + (ip[3] & 0xFF) + " Port: " + port);
				getConnection().setState(State.AUTHED);
				ChatService.setIp(ip);
				ChatService.setPort(port);
				break;
			case 1: // NotAuthed
				log.error("GameServer is not authenticated at ChatServer side");
				System.exit(ExitCode.CODE_ERROR);
				break;
			case 2: // AlreadyRegistered
				log.info("GameServer is already registered at ChatServer side! trying again...");
				ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						CM_CS_AUTH_RESPONSE.this.getConnection().sendPacket(new SM_CS_AUTH());
					}

				}, 10000);
				break;
		}
	}
}
