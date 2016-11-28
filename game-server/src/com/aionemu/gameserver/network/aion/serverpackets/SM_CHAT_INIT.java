package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public class SM_CHAT_INIT extends AionServerPacket {

	private byte[] token;

	/**
	 * @param token
	 */
	public SM_CHAT_INIT(byte[] token) {
		this.token = token;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(token.length);
		writeB(token);
	}
}
