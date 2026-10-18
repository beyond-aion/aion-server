package com.aionemu.chatserver.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.model.channel.ChatChannels;
import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.AionConnection;

import io.netty.buffer.ByteBuf;

/**
 * Request to leave a channel (sent on map change, logout or manually via /leavechannel)
 * 
 * @author Neon
 */
public class CM_CHANNEL_LEAVE extends AbstractClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_CHANNEL_LEAVE.class);
	private int channelId;

	public CM_CHANNEL_LEAVE(ByteBuf buf, AionConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		readB(16); // user ID, 0
		channelId = readD();
	}

	@Override
	protected void runImpl() {
		Channel channel = ChatChannels.getChannelById(channelId);
		if (!getConnection().getChatClient().removeChannel(channel))
			log.warn("{}, couldn't leave channel: {} (id: {})", getConnection().getChatClient(), channel, channelId);
	}
}
