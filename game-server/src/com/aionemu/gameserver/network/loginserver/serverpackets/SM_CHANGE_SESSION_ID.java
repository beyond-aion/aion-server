package com.aionemu.gameserver.network.loginserver.serverpackets;

import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;

/**
 * @author ViAl
 */
public class SM_CHANGE_SESSION_ID extends LsServerPacket {

	private int accountId;
	private String session;

	public SM_CHANGE_SESSION_ID(int accountId, String session) {
		super(20);
		this.accountId = accountId;
		this.session = session;
	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeD(accountId);
		writeS(session);
	}
}
