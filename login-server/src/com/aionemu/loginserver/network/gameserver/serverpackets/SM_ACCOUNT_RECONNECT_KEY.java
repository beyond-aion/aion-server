package com.aionemu.loginserver.network.gameserver.serverpackets;

import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsServerPacket;

/**
 * In this packet LoginServer is sending response for CM_ACCOUNT_RECONNECT_KEY with account name and reconnectionKey.
 * 
 * @author -Nemesiss-
 */
public class SM_ACCOUNT_RECONNECT_KEY extends GsServerPacket {

	/**
	 * accountId of account that will be reconnecting.
	 */
	private final int accountId;
	/**
	 * ReconnectKey that will be used for authentication.
	 */
	private final int reconnectKey;

	/**
	 * Constructor.
	 * 
	 * @param accountId
	 * @param reconnectKey
	 */
	public SM_ACCOUNT_RECONNECT_KEY(int accountId, int reconnectKey) {
		this.accountId = accountId;
		this.reconnectKey = reconnectKey;
	}

	@Override
	protected void writeImpl(GsConnection con) {
		writeC(3);
		writeD(accountId);
		writeD(reconnectKey);
	}
}
