package com.aionemu.chatserver.network.aion.serverpackets;

import com.aionemu.chatserver.model.message.Message;
import com.aionemu.chatserver.network.aion.AbstractServerPacket;
import com.aionemu.chatserver.network.aion.AionConnection;

/**
 * @author ATracer
 */
public class SM_CHANNEL_MESSAGE extends AbstractServerPacket {

	private final Message message;

	public SM_CHANNEL_MESSAGE(Message message) {
		super(0x1A);
		this.message = message;
	}

	@Override
	protected void writeImpl(AionConnection connection) {
		writeC(getOpCode());
		writeC(0x00);
		writeD(0x00);
		writeD(0x00);
		writeD(message.getChannel().getChannelId());
		writeD(message.getSender().getClientId());
		writeD(0x00);
		writeC(0x00);
		writeH(message.getSender().getIdentifier().length / 2);
		writeB(message.getSender().getIdentifier());
		writeH(message.size() / 2);
		writeB(message.getText());
	}
}
