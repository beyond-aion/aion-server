package com.aionemu.chatserver.common.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler.ClientChannelHandlerState;

/**
 * @author ATracer
 */
public abstract class AbstractPacketHandler {

	private static final Logger log = LoggerFactory.getLogger(AbstractPacketHandler.class);

	protected void logUnknownPacket(byte opCode, ClientChannelHandlerState state, ChannelBuffer buf) {
		int length = buf.readableBytes();
		StringBuilder sb = new StringBuilder(length * 3);
		while (buf.readable())
			sb.append("%02X".formatted(buf.readByte()));
		log.warn("Unknown packet received from client: opCode={} state={} length={} data=[{}]", "0x%02X".formatted(opCode), state, length, sb);
	}

}
