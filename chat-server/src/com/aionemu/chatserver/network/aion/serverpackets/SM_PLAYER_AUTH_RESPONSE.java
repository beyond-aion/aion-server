package com.aionemu.chatserver.network.aion.serverpackets;

import com.aionemu.chatserver.network.aion.AbstractServerPacket;
import com.aionemu.chatserver.network.aion.AionConnection;

/**
 * @author ATracer
 */
public class SM_PLAYER_AUTH_RESPONSE extends AbstractServerPacket {

	private final int msgIndex;

	public SM_PLAYER_AUTH_RESPONSE(int msgIndex) {
		super(0x4002);
		this.msgIndex = msgIndex;
	}

	@Override
	protected void writeImpl(AionConnection connection) {
		writeD(msgIndex);
		writeH(0); // result
		writeH(0x0822); // start value of the client's packet sequence numbers
	}
}
