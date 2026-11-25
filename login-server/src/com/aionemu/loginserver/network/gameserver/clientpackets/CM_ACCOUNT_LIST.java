package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.GameServerInfo;
import com.aionemu.loginserver.GameServerTable;
import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_HDDBAN_LIST;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_MACBAN_LIST;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_REQUEST_KICK_ACCOUNT;

/**
 * Reads the list of account id's that are logged in to game server. This packet is sent by game server once it successfully registered on this login
 * server.
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
		GameServerInfo ownGsi = getConnection().getGameServerInfo();
		if (ownGsi == null) // the link was closed while this packet waited
			return;
		for (int id : accountIds) {
			GameServerInfo gsi = GameServerTable.findLoggedInAccountGs(id);
			if (gsi == null) {
				Account account = AccountController.loadAccount(id);
				synchronized (AccountController.class) { // see GsConnection.onDisconnect
					if (getConnection().getGameServerInfo() == null)
						return;
					ownGsi.addAccountToGameServer(account);
				}
			} else if (gsi.getId() != ownGsi.getId()) { // account already plays on another gameserver
				getConnection().sendPacket(new SM_REQUEST_KICK_ACCOUNT(id, false));
			}
		}
		getConnection().sendPacket(new SM_MACBAN_LIST());
		getConnection().sendPacket(new SM_HDDBAN_LIST());
		AccountController.updateServerListForAllLoggedInPlayers();
	}
}
