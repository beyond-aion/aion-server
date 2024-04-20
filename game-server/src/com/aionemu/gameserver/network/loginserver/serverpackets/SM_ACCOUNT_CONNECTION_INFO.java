package com.aionemu.gameserver.network.loginserver.serverpackets;

import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;

/**
 * @author ViAl, Neon
 */
public class SM_ACCOUNT_CONNECTION_INFO extends LsServerPacket {

	private final int accountId;
	private final long time;
	private final String ip, mac, hddSerial;

	public SM_ACCOUNT_CONNECTION_INFO(int accountId, long time, String ip, String mac, String hddSerial) {
		super(7);
		this.accountId = accountId;
		this.time = time;
		this.ip = ip;
		this.mac = mac;
		this.hddSerial = hddSerial;
	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeD(accountId);
		writeQ(time);
		writeS(ip);
		writeS(mac);
		writeS(hddSerial);
	}
}
