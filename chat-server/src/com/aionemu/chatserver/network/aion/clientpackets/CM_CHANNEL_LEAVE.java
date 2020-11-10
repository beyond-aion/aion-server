package com.aionemu.chatserver.network.aion.clientpackets;

import org.jboss.netty.buffer.ChannelBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.model.channel.ChatChannels;
import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

/**
 * Request to leave a channel (sent on map change, logout or manually via /leavechannel)
 * 
 * @author Neon
 */
public class CM_CHANNEL_LEAVE extends AbstractClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_CHANNEL_LEAVE.class);
	private int channelId;

	public CM_CHANNEL_LEAVE(ChannelBuffer channelBuffer, ClientChannelHandler clientChannelHandler, byte opCode) {
		super(channelBuffer, clientChannelHandler, opCode);
	}

	@Override
	protected void readImpl() {
		readC(); // 0
		readH(); // 0
		readB(16); // 0
		channelId = readD();
	}

	@Override
	protected void runImpl() {
		Channel channel = ChatChannels.getChannelById(channelId);
		if (!clientChannelHandler.getChatClient().removeChannel(channel))
			log.warn("{}, couldn't leave channel: {} (id: {})", clientChannelHandler.getChatClient(), channel, channelId);
	}
}
