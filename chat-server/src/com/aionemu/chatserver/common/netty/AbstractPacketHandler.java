package com.aionemu.chatserver.common.netty;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler.State;

/**
 * @author ATracer
 */
public abstract class AbstractPacketHandler {

	private static final Logger log = LoggerFactory.getLogger(AbstractPacketHandler.class);

	/**
	 * Logs an unknown packet
	 * 
	 * @param opCode
	 * @param state
	 * @param buf
	 */
	protected void unknownPacket(byte opCode, State state, ChannelBuffer buf) {
		int length = buf.readableBytes();
		StringBuilder sb = new StringBuilder(length * 3);
		while (buf.readable())
			sb.append(String.format("%02X ", buf.readByte()));
		log.warn(String.format("Unknown packet received from client: opCode=0x%02X state=%s length=%d data=[%s]", opCode, state, length, sb.toString().trim()));
	}
}
