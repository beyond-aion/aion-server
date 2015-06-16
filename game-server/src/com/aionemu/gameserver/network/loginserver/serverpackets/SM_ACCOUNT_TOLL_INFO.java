package com.aionemu.gameserver.network.loginserver.serverpackets;

import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;

/**
 * @author xTz
 */
public class SM_ACCOUNT_TOLL_INFO extends LsServerPacket {

	private final long toll;

	private final String accountName;

	public SM_ACCOUNT_TOLL_INFO(long toll, String accountName) {
		super(0x09);
		this.accountName = accountName;
		this.toll = toll;
	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeQ(toll);
		writeS(accountName);
	}
}
