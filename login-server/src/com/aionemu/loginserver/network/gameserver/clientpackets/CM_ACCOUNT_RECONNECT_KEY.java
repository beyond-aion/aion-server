package com.aionemu.loginserver.network.gameserver.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.model.ReconnectingAccount;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_ACCOUNT_RECONNECT_KEY;

/**
 * This packet is sended by GameServer when player is requesting fast reconnect to login server. LoginServer in response will send reconectKey.
 * 
 * @author -Nemesiss-
 */
public class CM_ACCOUNT_RECONNECT_KEY extends GsClientPacket {

	/**
	 * Logger for this class.
	 */
	private static final Logger log = LoggerFactory.getLogger(CM_ACCOUNT_RECONNECT_KEY.class);
	/**
	 * accoundId of account that will be reconnecting.
	 */
	private int accountId;

	@Override
	protected void readImpl() {
		accountId = readD();
	}

	@Override
	protected void runImpl() {
		int reconectKey = Rnd.nextInt();
		Account acc = this.getConnection().getGameServerInfo().removeAccountFromGameServer(accountId);
		if (acc == null)
			log.info("This shouldnt happend! [Error]");
		else
			AccountController.addReconnectingAccount(new ReconnectingAccount(acc, reconectKey));
		sendPacket(new SM_ACCOUNT_RECONNECT_KEY(accountId, reconectKey));
	}
}
