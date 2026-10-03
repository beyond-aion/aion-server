package com.aionemu.chatserver.network.aion.clientpackets;

import java.nio.ByteBuffer;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.AionConnection;

/**
 * Request to join an existing private channel (via /joinchannel)
 * 
 * @author Neon
 */
public class CM_CHANNEL_JOIN extends AbstractClientPacket {

	@SuppressWarnings("unused")
	private int channelRequestId;
	@SuppressWarnings("unused")
	private byte[] channelIdentifier, password;

	public CM_CHANNEL_JOIN(ByteBuffer buf, AionConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		readUC(); // 0x40 = @
		readUH(); // 0
		channelRequestId = readD(); // client increases this by 1 for each request (e.g. after teleport)
		readB(16); // 0
		int identifierLength = readUH() * 2;
		channelIdentifier = readB(identifierLength); // encoded in UTF_16LE
		int passwordLength = readUH() * 2;
		password = readB(passwordLength); // encoded in UTF_16LE
	}

	@Override
	protected void runImpl() {
		// TODO see comments in CM_CHANNEL_CREATE
	}
}
