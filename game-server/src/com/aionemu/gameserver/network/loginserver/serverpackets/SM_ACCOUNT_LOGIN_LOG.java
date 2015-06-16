package com.aionemu.gameserver.network.loginserver.serverpackets;

import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;


/**
 * @author ViAl
 *
 */
public class SM_ACCOUNT_LOGIN_LOG extends LsServerPacket {

	private int accountId;
	private int gameserverId;
	private long time;
	private String ip;
	private String mac;
	private String hddSerial;
	/**
	 * @param opcode
	 * @param accountId
	 * @param gameserverId
	 * @param time
	 * @param ip
	 * @param mac
	 * @param hddSerial
	 */
	public SM_ACCOUNT_LOGIN_LOG(int accountId, int gameserverId, long time, String ip, String mac, String hddSerial) {
		super(17);
		this.accountId = accountId;
		this.gameserverId = gameserverId;
		this.time = time;
		this.ip = ip;
		this.mac = mac;
		this.hddSerial = hddSerial;
	}
	
	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeD(accountId);
		writeD(gameserverId);
		writeQ(time);
		writeS(ip);
		writeS(mac);
		writeS(hddSerial);
	}
	

}
