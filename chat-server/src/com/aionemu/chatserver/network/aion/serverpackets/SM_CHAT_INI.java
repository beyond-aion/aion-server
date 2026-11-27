package com.aionemu.chatserver.network.aion.serverpackets;

import com.aionemu.chatserver.network.aion.AbstractServerPacket;
import com.aionemu.chatserver.network.aion.AionConnection;

/**
 * @author ginho1
 */
public class SM_CHAT_INI extends AbstractServerPacket {

	private final int msgIndex;

	public SM_CHAT_INI(int msgIndex) {
		super(0x4031);
		this.msgIndex = msgIndex;
	}

	@Override
	protected void writeImpl(AionConnection connection) {
		writeD(msgIndex);
		writeH(0); // result: 0 = ok, 3 = unsupported version
	}
}
