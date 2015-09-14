package com.aionemu.chatserver.network.gameserver.serverpackets;

import com.aionemu.chatserver.configs.Config;
import com.aionemu.chatserver.network.gameserver.GsAuthResponse;
import com.aionemu.chatserver.network.gameserver.GsConnection;
import com.aionemu.chatserver.network.gameserver.GsServerPacket;

/**
 * @author ATracer
 */
public class SM_GS_AUTH_RESPONSE extends GsServerPacket {

	private GsAuthResponse response;

	public SM_GS_AUTH_RESPONSE(GsAuthResponse resp) {
		this.response = resp;
	}

	@Override
	protected void writeImpl(GsConnection con) {
		writeC(0);
		writeC(response.getResponseId());
		writeB(Config.CHAT_ADDRESS.getAddress().getAddress());
		writeH(Config.CHAT_ADDRESS.getPort());
	}
}
