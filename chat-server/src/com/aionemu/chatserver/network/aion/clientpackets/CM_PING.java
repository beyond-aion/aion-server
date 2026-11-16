package com.aionemu.chatserver.network.aion.clientpackets;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.AionConnection;

import io.netty.buffer.ByteBuf;

/**
 * Client sends this packet every 10 seconds after connecting to chat server
 * 
 * @author Neon
 */
public class CM_PING extends AbstractClientPacket {

	public CM_PING(ByteBuf buf, AionConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		readB(16); // 0
	}

	@Override
	protected void runImpl() {
	}
}
