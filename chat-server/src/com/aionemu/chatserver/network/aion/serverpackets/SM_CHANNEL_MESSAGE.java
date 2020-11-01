package com.aionemu.chatserver.network.aion.serverpackets;

import org.jboss.netty.buffer.ChannelBuffer;

import com.aionemu.chatserver.model.message.Message;
import com.aionemu.chatserver.network.aion.AbstractServerPacket;
import com.aionemu.chatserver.network.netty.handler.ClientChannelHandler;

/**
 * @author ATracer
 */
public class SM_CHANNEL_MESSAGE extends AbstractServerPacket {

	private final Message message;

	public SM_CHANNEL_MESSAGE(Message message) {
		super((byte) 0x1A);
		this.message = message;
	}

	@Override
	protected void writeImpl(ClientChannelHandler cHandler, ChannelBuffer buf) {
		writeC(buf, getOpCode());
		writeC(buf, 0x00);
		writeD(buf, 0x00);
		writeD(buf, 0x00);
		writeD(buf, message.getChannel().getChannelId());
		writeD(buf, message.getSender().getClientId());
		writeD(buf, 0x00);
		writeC(buf, 0x00);
		writeH(buf, message.getSender().getIdentifier().length / 2);
		writeB(buf, message.getSender().getIdentifier());
		writeH(buf, message.size() / 2);
		writeB(buf, message.getText());
	}
}
