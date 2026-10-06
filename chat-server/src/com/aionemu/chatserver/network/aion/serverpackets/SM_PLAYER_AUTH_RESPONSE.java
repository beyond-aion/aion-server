package com.aionemu.chatserver.network.aion.serverpackets;

import com.aionemu.chatserver.network.aion.AbstractServerPacket;
import com.aionemu.chatserver.network.aion.AionConnection;

/**
 * @author ATracer
 */
public class SM_PLAYER_AUTH_RESPONSE extends AbstractServerPacket {

	public SM_PLAYER_AUTH_RESPONSE() {
		super(0x02);
	}

	@Override
	protected void writeImpl(AionConnection connection) {
		writeC(getOpCode());
		writeC(0x40); // ?
		writeH(0x01); // ?
		writeD(0x00); // ?
		writeH(0x0822); // ?
	}
}
