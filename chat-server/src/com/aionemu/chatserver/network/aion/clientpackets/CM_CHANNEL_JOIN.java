package com.aionemu.chatserver.network.aion.clientpackets;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

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

	public CM_CHANNEL_JOIN(ChannelBuffer channelBuffer, ClientChannelHandler clientChannelHandler, byte opCode) {
		super(channelBuffer, clientChannelHandler, opCode);
	}

	@Override
	protected void readImpl() {
		readC(); // 0x40 = @
		readH(); // 0
		channelRequestId = readD(); // client increases this by 1 for each request (e.g. after teleport)
		readB(16); // 0
		int identifierLength = readH() * 2;
		channelIdentifier = readB(identifierLength); // encoded in UTF_16LE
		int passwordLength = readH() * 2;
		password = readB(passwordLength); // encoded in UTF_16LE
	}

	@Override
	protected void runImpl() {
		// TODO see comments in CM_CHANNEL_CREATE
	}
}
