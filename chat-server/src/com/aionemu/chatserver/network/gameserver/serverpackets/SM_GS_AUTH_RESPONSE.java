package com.aionemu.chatserver.network.gameserver.serverpackets;

import com.aionemu.chatserver.configs.network.NetworkConfig;
import com.aionemu.chatserver.network.gameserver.GsAuthResponse;
import com.aionemu.chatserver.network.gameserver.GsConnection;
import com.aionemu.chatserver.network.gameserver.GsServerPacket;

/**
 * @author ATracer
 */
public class SM_GS_AUTH_RESPONSE extends GsServerPacket {

	private final GsAuthResponse response;

	public SM_GS_AUTH_RESPONSE(GsAuthResponse rsp) {
		response = rsp;
	}

	@Override
	protected void writeImpl(GsConnection con) {
		writeC(0);
		writeC(response.getResponseId());
		if (response == GsAuthResponse.AUTHED) {
			byte[] csIp = NetworkConfig.CLIENT_CONNECT_ADDRESS.getAddress().getAddress();
			writeC(csIp.length);
			writeB(csIp);
			writeH(NetworkConfig.CLIENT_CONNECT_ADDRESS.getPort());
		}
	}
}
