package com.aionemu.chatserver.network.aion.serverpackets;

import com.aionemu.chatserver.network.aion.AbstractServerPacket;
import com.aionemu.chatserver.network.aion.AionConnection;

/**
 * @author ginho1
 */
public class SM_CHAT_INI extends AbstractServerPacket {

	public SM_CHAT_INI() {
		super(0x31);
	}

	@Override
	protected void writeImpl(AionConnection connection) {
		writeC(getOpCode());
		writeC(0x40);
		writeD(0x02);
		writeH(0x00);
	}
}
