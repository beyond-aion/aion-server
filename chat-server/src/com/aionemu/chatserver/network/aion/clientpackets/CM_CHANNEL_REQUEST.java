package com.aionemu.chatserver.network.aion.clientpackets;

import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;
import com.aionemu.chatserver.service.ChatService;

/**
 * Request to join a system or language channel (language channels are actually user/private channels)
 * 
 * @author ATracer
 */
public class CM_CHANNEL_REQUEST extends AbstractClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_CHANNEL_REQUEST.class);
	private int channelRequestId;
	private byte[] channelIdentifier;

	public CM_CHANNEL_REQUEST(ChannelBuffer channelBuffer, ClientChannelHandler clientChannelHandler, byte opCode) {
		super(channelBuffer, clientChannelHandler, opCode);
	}

	@Override
	protected void readImpl() {
		readC(); // 0x40 = @
		readH(); // 0
		channelRequestId = readD(); // client increases this by 1 for each request (e.g. after teleport)
		readB(16); // 0
		int length = (readH() * 2);
		channelIdentifier = readB(length);
		readD(); // 0
	}

	@Override
	protected void runImpl() {
		try {
			ChatService.getInstance().registerPlayerWithChannel(clientChannelHandler, channelRequestId, new String(channelIdentifier, "UTF-16le"));
		} catch (UnsupportedEncodingException e) {
			log.error("Could not read channel identifier from: " + new String(channelIdentifier));
		}
	}
}
