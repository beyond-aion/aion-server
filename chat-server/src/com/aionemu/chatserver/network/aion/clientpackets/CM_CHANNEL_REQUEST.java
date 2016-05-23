package com.aionemu.chatserver.network.aion.clientpackets;

import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;
import com.aionemu.chatserver.service.ChatService;

/**
 * @author ATracer
 */
public class CM_CHANNEL_REQUEST extends AbstractClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_CHANNEL_REQUEST.class);
	private int channelRequestId;
	private byte[] channelIdentifier;

	/**
	 * @param channelBuffer
	 * @param gameChannelHandler
	 * @param opCode
	 */
	public CM_CHANNEL_REQUEST(ChannelBuffer channelBuffer, ClientChannelHandler gameChannelHandler) {
		super(channelBuffer, gameChannelHandler, 0x10);
	}

	@Override
	protected void readImpl() {
		readC(); // 0x40 = @
		readH(); // 0
		channelRequestId = readH(); // client increases this by 1 for each request (e.g. after teleport)
		readB(18); // ?
		int length = (readH() * 2);
		channelIdentifier = readB(length);
		readD(); // ?
	}

	@Override
	protected void runImpl() {
		try {
			ChatService.getInstance().registerPlayerWithChannel(clientChannelHandler, channelRequestId, new String(channelIdentifier, "UTF-16le"));
		} catch (UnsupportedEncodingException e) {
			log.error("Could not read channel name from: " + channelIdentifier);
		}
	}

	@Override
	public String toString() {
		return "CM_CHANNEL_REQUEST [channelRequestId=" + channelRequestId + ", channelIdentifier=" + new String(channelIdentifier) + "]";
	}
}
