package com.aionemu.gameserver.network.loginserver.clientpackets;

import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.network.loginserver.LsClientPacket;

/**
 * In this packet LoginServer is sending response for SM_ACCOUNT_RECONNECT_KEY with account name and reconnectionKey.
 * 
 * @author -Nemesiss-
 */
public class CM_ACCOUNT_RECONNECT_KEY extends LsClientPacket {

	public CM_ACCOUNT_RECONNECT_KEY(int opCode) {
		super(opCode);
	}

	/**
	 * accountId of account that will be reconnecting.
	 */
	private int accountId;
	/**
	 * ReconnectKey that will be used for authentication.
	 */
	private int reconnectKey;

	@Override
	public void readImpl() {
		accountId = readD();
		reconnectKey = readD();
	}

	@Override
	public void runImpl() {
		LoginServer.getInstance().authReconnectionResponse(accountId, reconnectKey);
	}
}
