package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Artur
 */
public class SM_GF_WEBSHOP_TOKEN_RESPONSE extends AionServerPacket {

	private final String token;

	public SM_GF_WEBSHOP_TOKEN_RESPONSE(String token) {
		this.token = token;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeS(token, 32);
	}
}
