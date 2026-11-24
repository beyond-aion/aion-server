package com.aionemu.chatserver.network.gameserver.clientpackets;

import com.aionemu.chatserver.network.gameserver.GsClientPacket;
import com.aionemu.chatserver.network.gameserver.GsConnection;
import com.aionemu.chatserver.service.ChatService;

import io.netty.buffer.ByteBuf;

/**
 * @author ATracer
 */
public class CM_PLAYER_LOGOUT extends GsClientPacket {

	private int playerId;

	public CM_PLAYER_LOGOUT(ByteBuf buf, GsConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		playerId = readD();
	}

	@Override
	protected void runImpl() {
		ChatService.getInstance().playerLogout(playerId);
	}
}
