package com.aionemu.loginserver.network.gameserver.serverpackets;

import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsServerPacket;


/**
 * @author Aionchs-Wylovech
 */
public class SM_LS_CONTROL_RESPONSE extends GsServerPacket {

	private int type;

	private boolean result;

	private String playerName;

	private int param;

	private String adminName;

	private int accountId;

	public SM_LS_CONTROL_RESPONSE(int type, boolean result, String playerName, int accountId, int param, String adminName) {
		this.type = type;
		this.result = result;
		this.playerName = playerName;
		this.param = param;
		this.adminName = adminName;
		this.accountId = accountId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(GsConnection con) {
		writeC(4);
		writeC(type);
		writeC(result ? 1 : 0);
		writeS(adminName);
		writeS(playerName);
		writeC(param);
		writeD(accountId);
	}
}
