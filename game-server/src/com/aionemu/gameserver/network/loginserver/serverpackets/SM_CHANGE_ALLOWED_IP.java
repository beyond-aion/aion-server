package com.aionemu.gameserver.network.loginserver.serverpackets;

import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;


/**
 * @author ViAl
 *
 */
public class SM_CHANGE_ALLOWED_IP extends LsServerPacket {

	private int accountId;
	private String ip;
	
	public SM_CHANGE_ALLOWED_IP(int accountId, String ip) {
		super(19);
		this.accountId = accountId;
		this.ip = ip;
	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeD(accountId);
		writeS(ip);
	}
}
