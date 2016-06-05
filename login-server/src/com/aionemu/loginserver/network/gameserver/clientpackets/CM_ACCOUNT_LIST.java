package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.GameServerTable;
import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_REQUEST_KICK_ACCOUNT;

/**
 * Reads the list of account id's that are logged to game server
 * 
 * @author SoulKeeper, Neon
 */
public class CM_ACCOUNT_LIST extends GsClientPacket {

	/**
	 * Array with accounts that are logged in
	 */
	private int[] accountIds;

	@Override
	protected void readImpl() {
		accountIds = new int[readD()];
		for (int i = 0; i < accountIds.length; i++)
			accountIds[i] = readD();
	}

	@Override
	protected void runImpl() {
		for (int id : accountIds) {
			Account a = AccountController.loadAccount(id);
			if (GameServerTable.isAccountOnAnyGameServer(a)) {
				getConnection().sendPacket(new SM_REQUEST_KICK_ACCOUNT(id, false));
				continue;
			}
			getConnection().getGameServerInfo().addAccountToGameServer(a);
		}
	}
}
