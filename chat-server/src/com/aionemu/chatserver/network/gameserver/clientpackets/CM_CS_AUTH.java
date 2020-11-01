package com.aionemu.chatserver.network.gameserver.clientpackets;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.network.gameserver.GsAuthResponse;
import com.aionemu.chatserver.network.gameserver.GsClientPacket;
import com.aionemu.chatserver.network.gameserver.GsConnection;
import com.aionemu.chatserver.network.gameserver.GsConnection.GameServerConnectionState;
import com.aionemu.chatserver.network.gameserver.serverpackets.SM_GS_AUTH_RESPONSE;
import com.aionemu.chatserver.service.GameServerService;

/**
 * @author ATracer
 */
public class CM_CS_AUTH extends GsClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_CS_AUTH.class);
	private String password;
	private byte gameServerId;

	public CM_CS_AUTH(ByteBuffer buf, GsConnection connection) {
		super(buf, connection, 0x00);
	}

	@Override
	protected void readImpl() {
		gameServerId = readC();
		password = readS();
	}

	@Override
	protected void runImpl() {
		GsAuthResponse resp = GameServerService.getInstance().registerGameServer(gameServerId, password);
		switch (resp) {
			case AUTHED -> {
				getConnection().setState(GameServerConnectionState.AUTHED);
				log.info("Gameserver #{} is now online", gameServerId);
			}
			case NOT_AUTHED -> log.warn("Gameserver #{} (IP: {}) tried to register with an invalid password", gameServerId, getConnection().getIP());
			case ALREADY_REGISTERED -> log.info("Gameserver #{} is already registered", gameServerId);
		}
		sendPacket(new SM_GS_AUTH_RESPONSE(resp));
	}
}
