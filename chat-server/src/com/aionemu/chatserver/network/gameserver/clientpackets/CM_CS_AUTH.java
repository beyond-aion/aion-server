package com.aionemu.chatserver.network.gameserver.clientpackets;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.network.gameserver.GsAuthResponse;
import com.aionemu.chatserver.network.gameserver.GsClientPacket;
import com.aionemu.chatserver.network.gameserver.GsConnection;
import com.aionemu.chatserver.network.gameserver.GsConnection.State;
import com.aionemu.chatserver.network.gameserver.serverpackets.SM_GS_AUTH_RESPONSE;
import com.aionemu.chatserver.service.GameServerService;

/**
 * @author ATracer
 */
public class CM_CS_AUTH extends GsClientPacket {

	private Logger log = LoggerFactory.getLogger(CM_CS_AUTH.class);
	/**
	 * Password for authentication
	 */
	private String password;

	/**
	 * Id of GameServer
	 */
	private byte gameServerId;

	/**
	 * Default address for server
	 */
	private byte[] defaultAddress;

	public CM_CS_AUTH(ByteBuffer buf, GsConnection connection) {
		super(buf, connection, 0x00);
	}

	@Override
	protected void readImpl() {
		gameServerId = (byte) readC();
		defaultAddress = readB(readC());
		password = readS();
	}

	@Override
	protected void runImpl() {
		GsAuthResponse resp = GameServerService.getInstance().registerGameServer(gameServerId, defaultAddress, password);

		switch (resp) {
			case AUTHED:
				getConnection().setState(State.AUTHED);
				getConnection().sendPacket(new SM_GS_AUTH_RESPONSE(resp));
				log.info("Gameserver #" + gameServerId + " is now online.");
				break;
			case NOT_AUTHED:
				log.warn("Gameserver #" + gameServerId + " has invalid password.");
				getConnection().sendPacket(new SM_GS_AUTH_RESPONSE(resp));
				break;
			case ALREADY_REGISTERED:
				log.info("Gameserver #" + gameServerId + " is already registered!");
				getConnection().sendPacket(new SM_GS_AUTH_RESPONSE(resp));
				break;
		}
	}
}
