package com.aionemu.loginserver.network.gameserver.serverpackets;

import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsServerPacket;


/**
 * In this packet LoginSerer is requesting kicking account from GameServer.
 * 
 * @author -Nemesiss-
 */
public class SM_REQUEST_KICK_ACCOUNT extends GsServerPacket {

	/**
	 * Account that must be kicked at GameServer side.
	 */
	private final int accountId;

	/**
	 * Constructor.
	 * 
	 * @param accountId
	 */
	public SM_REQUEST_KICK_ACCOUNT(int accountId) {
		this.accountId = accountId;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(GsConnection con) {
		writeC(2);
		writeD(accountId);
	}
}
