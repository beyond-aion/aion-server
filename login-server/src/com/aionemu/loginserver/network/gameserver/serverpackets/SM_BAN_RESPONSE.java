package com.aionemu.loginserver.network.gameserver.serverpackets;

import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsServerPacket;

/**
 * In this packet LoginServer is answering on GameServer ban request
 * 
 * @author Watson
 */
public class SM_BAN_RESPONSE extends GsServerPacket {

	private final byte type;
	private final int accountId;
	private final String ip;
	private final int time;
	private final int adminObjId;
	private final boolean result;

	public SM_BAN_RESPONSE(byte type, int accountId, String ip, int time, int adminObjId, boolean result) {
		this.type = type;
		this.accountId = accountId;
		this.ip = ip;
		this.time = time;
		this.adminObjId = adminObjId;
		this.result = result;
	}

	@Override
	protected void writeImpl(GsConnection con) {
		writeC(5);
		writeC(type);
		writeD(accountId);
		writeS(ip);
		writeD(time);
		writeD(adminObjId);
		writeC(result ? 1 : 0);
	}
}
