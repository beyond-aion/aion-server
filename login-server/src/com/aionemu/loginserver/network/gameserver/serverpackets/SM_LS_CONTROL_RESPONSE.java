package com.aionemu.loginserver.network.gameserver.serverpackets;

import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsServerPacket;

/**
 * @author Aionchs-Wylovech
 */
public class SM_LS_CONTROL_RESPONSE extends GsServerPacket {

	private final byte type, param;
	private final int accountId, adminId;
	private boolean result;

	public SM_LS_CONTROL_RESPONSE(byte type, byte param, int accountId, int adminId, boolean result) {
		this.type = type;
		this.param = param;
		this.accountId = accountId;
		this.adminId = adminId;
		this.result = result;
	}

	@Override
	protected void writeImpl(GsConnection con) {
		writeC(4);
		writeC(type);
		writeC(param);
		writeD(accountId);
		writeD(adminId);
		writeC(result ? 1 : 0);
	}
}
