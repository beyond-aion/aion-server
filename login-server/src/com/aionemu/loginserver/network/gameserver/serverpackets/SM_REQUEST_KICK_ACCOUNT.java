package com.aionemu.loginserver.network.gameserver.serverpackets;

import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsServerPacket;

/**
 * In this packet LoginSerer is requesting kicking account from GameServer.
 * 
 * @author -Nemesiss-, Neon
 */
public class SM_REQUEST_KICK_ACCOUNT extends GsServerPacket {

	private final int accountId;
	private final boolean notifyDoubleLogin;

	/**
	 * @param accountId
	 *          - account that must be kicked at GameServer side
	 * @param notifyDoubleLogin
	 *          - whether to notify the player that he got kicked due to another client logging in
	 */
	public SM_REQUEST_KICK_ACCOUNT(int accountId, boolean notifyDoubleLogin) {
		this.accountId = accountId;
		this.notifyDoubleLogin = notifyDoubleLogin;
	}

	@Override
	protected void writeImpl(GsConnection con) {
		writeC(2);
		writeD(accountId);
		writeC(notifyDoubleLogin ? 1 : 0);
	}
}
