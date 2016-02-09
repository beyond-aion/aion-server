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
		if (response == GsAuthResponse.AUTHED) {
			byte[] csIp = Config.CLIENT_CONNECT_ADDRESS.getAddress().getAddress();
			writeC(csIp.length);
			writeB(csIp);
			writeH(Config.CLIENT_CONNECT_ADDRESS.getPort());
		}
	}
}
