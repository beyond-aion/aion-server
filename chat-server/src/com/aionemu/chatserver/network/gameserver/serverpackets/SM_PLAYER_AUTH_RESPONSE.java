package com.aionemu.chatserver.network.gameserver.serverpackets;

import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.network.gameserver.GsConnection;
import com.aionemu.chatserver.network.gameserver.GsServerPacket;

/**
 * @author ATracer
 */
public class SM_PLAYER_AUTH_RESPONSE extends GsServerPacket {

	private final int playerId;
	private final byte[] token;

	public SM_PLAYER_AUTH_RESPONSE(ChatClient chatClient) {
		playerId = chatClient.getClientId();
		token = chatClient.getToken();
	}

	@Override
	protected void writeImpl(GsConnection con) {
		writeC(1);
		writeD(playerId);
		writeC(token.length);
		writeB(token);
	}
}
