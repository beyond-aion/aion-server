package com.aionemu.chatserver.network.aion.clientpackets;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.model.channel.ChatChannels;
import com.aionemu.chatserver.network.aion.AbstractClientPacket;
import com.aionemu.chatserver.network.aion.AionConnection;

/**
 * Request to leave a channel (sent on map change, logout or manually via /leavechannel)
 * 
 * @author Neon
 */
public class CM_CHANNEL_LEAVE extends AbstractClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_CHANNEL_LEAVE.class);
	private int channelId;

	public CM_CHANNEL_LEAVE(ByteBuffer buf, AionConnection connection, int opCode) {
		super(buf, connection, opCode);
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
		if (!getConnection().getChatClient().removeChannel(channel))
			log.warn("{}, couldn't leave channel: {} (id: {})", getConnection().getChatClient(), channel, channelId);
	}
}
