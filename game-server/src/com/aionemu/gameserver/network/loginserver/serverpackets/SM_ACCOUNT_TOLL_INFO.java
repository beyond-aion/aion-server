package com.aionemu.gameserver.network.loginserver.serverpackets;

import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;

/**
 * @author xTz
 */
public class SM_ACCOUNT_TOLL_INFO extends LsServerPacket {

	private final int accountId;
	private final long toll;

	public SM_ACCOUNT_TOLL_INFO(long toll, int accountId) {
		super(0x09);
		this.accountId = accountId;
		this.toll = toll;
	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeD(accountId);
		writeQ(toll);
	}
}
