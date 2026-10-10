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
		super(0x11);
		this.channelId = channel.getChannelId();
		this.channelRequestId = channelRequestId;
	}

	@Override
	protected void writeImpl(AionConnection connection) {
		writeC(getOpCode());
		writeC(0x40);
		writeD(channelRequestId);
		writeH(0x00);
		writeD(channelId);
	}
}
