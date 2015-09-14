package com.aionemu.chatserver.network.aion.clientpackets;

import java.io.UnsupportedEncodingException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.configs.Config;
import com.aionemu.chatserver.model.ChatClient;
import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.serverpackets.SM_CHANNEL_RESPONSE;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;
import com.aionemu.chatserver.service.ChatService;

/**
 * @author ATracer
 */
public class CM_CHANNEL_REQUEST extends AbstractClientPacket
{
	private static final Logger log = LoggerFactory.getLogger(CM_CHANNEL_REQUEST.class);
	private int channelIndex;
	private byte[] channelIdentifier;
	private ChatService chatService;

	/**
	 * @param channelBuffer
	 * @param gameChannelHandler
	 * @param opCode
	 */
	public CM_CHANNEL_REQUEST(ChannelBuffer channelBuffer, ClientChannelHandler gameChannelHandler, ChatService chatService)
	{
		super(channelBuffer, gameChannelHandler, 0x10);
		this.chatService = chatService;
	}

	@Override
	protected void readImpl()
	{
		readC(); // 0x40
		readH(); // 0x00
		channelIndex = readH();
		readB(18); //?
		int length = (readH() * 2);
		channelIdentifier = readB(length);
		readD(); // ?
	}

	@Override
	protected void runImpl()
	{
		try
		{
			if (Config.LOG_CHANNEL_REQUEST)
			{
				log.info("Channel requested " + new String(channelIdentifier, "UTF-16le"));
			}
		}
		catch (UnsupportedEncodingException e)
		{
			e.printStackTrace();
		}
		ChatClient chatClient = clientChannelHandler.getChatClient();
		Channel channel = chatService.registerPlayerWithChannel(chatClient, channelIndex, channelIdentifier);
		if (channel != null)
		{
			clientChannelHandler.sendPacket(new SM_CHANNEL_RESPONSE(channel, channelIndex));
		}
	}

	@Override
	public String toString()
	{
		return "CM_CHANNEL_REQUEST [channelIndex=" + channelIndex + ", channelIdentifier=" + new String(channelIdentifier) + "]";
	}
}
