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

	public CM_PLAYER_LOGOUT(ByteBuf buf, GsConnection connection) {
		super(buf, connection, 0x02);
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
