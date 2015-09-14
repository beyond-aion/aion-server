package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Artur
 */
public class SM_SECURITY_TOKEN_REQUEST_STATUS extends AionServerPacket {

	private String token;

	public SM_SECURITY_TOKEN_REQUEST_STATUS(String token) {
		this.token = token;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeS(token, 64);
	}

}
