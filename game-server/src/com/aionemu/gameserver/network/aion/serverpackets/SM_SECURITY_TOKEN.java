package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ginho1
 */
public class SM_SECURITY_TOKEN extends AionServerPacket {

	private byte[] token;

	public SM_SECURITY_TOKEN(byte[] token) {
		this.token = token;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(0x0);// NA(0),EU(3)
		writeB(token);
		writeB(new byte[token.length]);
	}
}
