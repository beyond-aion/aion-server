package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.network.aion.SessionKey;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;

/**
 * In this packet Gameserver is asking if given account sessionKey is valid at Loginserver side. [if user that is authenticating on Gameserver is
 * already authenticated on Loginserver]
 * 
 * @author -Nemesiss-
 */
public class CM_ACCOUNT_AUTH extends GsClientPacket {

	/**
	 * SessionKey that GameServer needs to check if is valid at Loginserver side.
	 */
	private SessionKey sessionKey;

	@Override
	protected void readImpl() {
		int accountId = readD();
		int loginOk = readD();
		int playOk1 = readD();
		int playOk2 = readD();

		sessionKey = new SessionKey(accountId, loginOk, playOk1, playOk2);
	}

	@Override
	protected void runImpl() {
		AccountController.checkAuth(sessionKey, this.getConnection());
	}
}
