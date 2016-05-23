package com.aionemu.chatserver.network.aion.clientpackets;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

/**
 * Client sends this packet every 10 seconds after connecting to chat server
 * 
 * @author Neon
 */
public class CM_PING extends AbstractClientPacket {

	public CM_PING(ChannelBuffer channelBuffer, ClientChannelHandler clientChannelHandler, byte opCode) {
		super(channelBuffer, clientChannelHandler, opCode);
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
