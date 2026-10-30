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
		writeQ(Integer.toUnsignedLong(connection.getChatClient().getClientId())); // recipient
		writeD(message.getChannel().getChannelId());
		writeQ(Integer.toUnsignedLong(message.getSender().getClientId()));
		writeC(0); // user type
		writeH(message.getSender().getIdentifier().length / 2);
		writeB(message.getSender().getIdentifier());
		writeH(message.size() / 2);
		writeB(message.getText());
	}
}
