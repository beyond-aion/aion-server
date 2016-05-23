package com.aionemu.chatserver.network.aion.serverpackets;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.network.aion.AbstractServerPacket;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

/**
 * @author ATracer
 */
public class SM_CHANNEL_RESPONSE extends AbstractServerPacket {

	private final int channelId;
	private final int channelRequestId;

	public SM_CHANNEL_RESPONSE(Channel channel, int channelRequestId) {
		super((byte) 0x11);
		this.channelId = channel.getChannelId();
		this.channelRequestId = channelRequestId;
	}

	@Override
	protected void writeImpl(ClientChannelHandler cHandler, ChannelBuffer buf) {
		writeC(buf, getOpCode());
		writeC(buf, 0x40);
		writeD(buf, channelRequestId);
		writeH(buf, 0x00);
		writeD(buf, channelId);
	}
}
