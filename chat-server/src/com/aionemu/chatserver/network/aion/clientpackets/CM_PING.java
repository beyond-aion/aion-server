package com.aionemu.chatserver.network.aion.clientpackets;

import java.nio.ByteBuffer;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.AionConnection;

/**
 * Client sends this packet every 10 seconds after connecting to chat server
 * 
 * @author Neon
 */
public class CM_PING extends AbstractClientPacket {

	public CM_PING(ByteBuffer buf, AionConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		readC(); // 0
		readH(); // 0
		readB(16); // 0
	}

	@Override
	protected void runImpl() {
	}
}
