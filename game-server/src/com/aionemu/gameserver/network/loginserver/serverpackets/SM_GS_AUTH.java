package com.aionemu.gameserver.network.loginserver.serverpackets;

import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;

/**
 * This is authentication packet that GS will send to login server for registration.
 * 
 * @author -Nemesiss-, Neon
 */
public class SM_GS_AUTH extends LsServerPacket {

	public SM_GS_AUTH() {
		super(0x00);
	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		byte[] gsIp = NetworkConfig.CLIENT_CONNECT_ADDRESS.getAddress().getAddress();
		writeC(NetworkConfig.GAMESERVER_ID);
		writeC(gsIp.length);
		writeB(gsIp);
		writeH(NetworkConfig.CLIENT_CONNECT_ADDRESS.getPort());
		writeD(NetworkConfig.MAX_ONLINE_PLAYERS);
		writeS(NetworkConfig.LOGIN_PASSWORD);
	}
}
