package com.aionemu.gameserver.network.loginserver.serverpackets;

import com.aionemu.gameserver.configs.network.NetworkConfig;
import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;

/**
 * 
 * @author KID
 *
 */
public class SM_LS_PONG extends LsServerPacket {
	private int pid;
	
	public SM_LS_PONG(int pid) {
		super(12);
		this.pid = pid;
	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeC(NetworkConfig.GAMESERVER_ID);
		writeD(pid);
	}
}
