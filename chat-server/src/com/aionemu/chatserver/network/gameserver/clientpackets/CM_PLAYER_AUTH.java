package com.aionemu.chatserver.network.gameserver.clientpackets;

import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.model.Race;
import com.aionemu.chatserver.network.gameserver.GsClientPacket;
import com.aionemu.chatserver.network.gameserver.GsConnection;
import com.aionemu.chatserver.network.gameserver.serverpackets.SM_PLAYER_AUTH_RESPONSE;
import com.aionemu.chatserver.service.ChatService;

/**
 * @author ATracer
 */
public class CM_PLAYER_AUTH extends GsClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_PLAYER_AUTH.class);
	private int playerId;
	private String accName;
	private String nick;
	private int raceId;
	private byte accessLevel;

	public CM_PLAYER_AUTH(ByteBuffer buf, GsConnection connection) {
		super(buf, connection, 0x01);
	}

	@Override
	protected void readImpl() {
		playerId = readD();
		accName = readS();
		nick = readS();
		raceId = readD();
		accessLevel = readC();
	}

	@Override
	protected void runImpl() {
		try {
			ChatClient chatClient = ChatService.getInstance().registerPlayer(playerId, accName, nick, Race.getById(raceId), accessLevel);
			sendPacket(new SM_PLAYER_AUTH_RESPONSE(chatClient));
		} catch (NoSuchAlgorithmException e) {
			log.error("Error registering player {} on ChatServer", playerId, e);
		}
	}
}
