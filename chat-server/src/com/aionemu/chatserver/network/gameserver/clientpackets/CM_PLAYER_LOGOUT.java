package com.aionemu.chatserver.network.gameserver.clientpackets;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.network.gameserver.GsClientPacket;
import com.aionemu.chatserver.network.gameserver.GsConnection;
import com.aionemu.chatserver.service.ChatService;

/**
 * @author ATracer
 */
public class CM_PLAYER_LOGOUT extends GsClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_PLAYER_LOGOUT.class);
	private int playerId;

	public CM_PLAYER_LOGOUT(ByteBuffer buf, GsConnection connection) {
		super(buf, connection, 0x02);
	}

	@Override
	protected void readImpl() {
		playerId = readD();
	}

	@Override
	protected void runImpl() {
		ChatService.getInstance().playerLogout(playerId);
		log.info("Player logout " + playerId);
	}
}
