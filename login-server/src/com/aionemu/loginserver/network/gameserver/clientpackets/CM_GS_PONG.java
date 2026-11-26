package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.GsConnection;

import io.netty.buffer.ByteBuf;

/**
 * @author KID
 */
public class CM_GS_PONG extends GsClientPacket {

	public CM_GS_PONG(ByteBuf buf, GsConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
		getConnection().getPingPongTask().onReceivePong();
	}
}
