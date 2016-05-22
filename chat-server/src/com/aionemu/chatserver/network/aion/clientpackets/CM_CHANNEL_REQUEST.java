package com.aionemu.chatserver.network.aion.clientpackets;

import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.main.LoggingConfig;
import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.serverpackets.SM_CHANNEL_RESPONSE;
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
		readC(); // 0x40
		readH(); // 0x00
		channelRequestId = readH(); // client increases this by 1 for each request (e.g. after teleport)
		readB(18); // ?
		int length = (readH() * 2);
		channelIdentifier = readB(length);
		readD(); // ?
	}

	@Override
	protected void runImpl() {
		try {
			String name = new String(channelIdentifier, "UTF-16le");
			if (LoggingConfig.LOG_CHANNEL_REQUEST)
				log.info("Channel requested: " + name); // e.g. @partyFind_PF1.0.AION.KOR where 1 is the server id and 0 race id
			Channel channel = ChatService.getInstance().registerPlayerWithChannel(clientChannelHandler.getChatClient(), name);
			if (channel != null)
				clientChannelHandler.sendPacket(new SM_CHANNEL_RESPONSE(channel, channelRequestId));
		} catch (UnsupportedEncodingException e) {
			log.error("Could not read channel name from: " + channelIdentifier);
		}
	}

	@Override
	public String toString() {
		return "CM_CHANNEL_REQUEST [channelRequestId=" + channelRequestId + ", channelIdentifier=" + new String(channelIdentifier) + "]";
	}
}
