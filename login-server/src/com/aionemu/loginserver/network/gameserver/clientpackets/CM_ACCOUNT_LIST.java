package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.GameServerTable;
import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_REQUEST_KICK_ACCOUNT;

/**
 * Reads the list of accoutn id's that are logged to game server
 * 
 * @author SoulKeeper
 */
public class CM_ACCOUNT_LIST extends GsClientPacket {

	/**
	 * Array with accounts that are logged in
	 */
	private String[] accountNames;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		accountNames = new String[readD()];
		for (int i = 0; i < accountNames.length; i++) {
			accountNames[i] = readS();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		for (String s : accountNames) {
			Account a = AccountController.loadAccount(s);
			if (GameServerTable.isAccountOnAnyGameServer(a)) {
				this.getConnection().sendPacket(new SM_REQUEST_KICK_ACCOUNT(a.getId()));
				continue;
			}
			getConnection().getGameServerInfo().addAccountToGameServer(a);
		}
	}
}
