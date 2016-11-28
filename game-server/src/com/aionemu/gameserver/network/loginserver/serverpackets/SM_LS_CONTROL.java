package com.aionemu.gameserver.network.loginserver.serverpackets;

import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;

/**
 * @author Aionchs-Wylovech
 */
public class SM_LS_CONTROL extends LsServerPacket {

	private final String accountName;

	private final String adminName;

	private final String playerName;

	private final int param;

	private final int type;

	public SM_LS_CONTROL(String accountName, String playerName, String adminName, int param, int type) {
		super(0x05);
		this.accountName = accountName;
		this.param = param;
		this.playerName = playerName;
		this.adminName = adminName;
		this.type = type;

	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeC(type);
		writeS(adminName);
		writeS(accountName);
		writeS(playerName);
		writeC(param);
	}
}
