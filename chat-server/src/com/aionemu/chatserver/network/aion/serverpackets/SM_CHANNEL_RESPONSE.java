package com.aionemu.chatserver.network.aion.serverpackets;

import com.aionemu.chatserver.model.channel.Channel;
import com.aionemu.chatserver.network.aion.AbstractServerPacket;
import com.aionemu.chatserver.network.aion.AionConnection;

/**
 * @author ATracer
 */
public class SM_CHANNEL_RESPONSE extends AbstractServerPacket {

	private final int channelId;
	private final int channelRequestId;

	public SM_CHANNEL_RESPONSE(Channel channel, int channelRequestId) {
		super(0x4011);
		this.channelId = channel.getChannelId();
		this.channelRequestId = channelRequestId;
	}

	@Override
	protected void writeImpl(AionConnection connection) {
		writeD(channelRequestId);
		writeH(0); // result
		writeD(channelId);
	}
}
