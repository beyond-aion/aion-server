package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.GameServerInfo;
import com.aionemu.loginserver.controller.AccountTimeController;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.GsConnection;

import io.netty.buffer.ByteBuf;

/**
 * In this packet GameServer is informing LoginServer that some account is no longer on GameServer [ie was disconencted]
 * 
 * @author -Nemesiss-
 */
public class CM_ACCOUNT_DISCONNECTED extends GsClientPacket {

	/**
	 * AccountId of account that was disconnected form GameServer.
	 */
	private int accountId;

	public CM_ACCOUNT_DISCONNECTED(ByteBuf buf, GsConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		accountId = readD();
	}

	@Override
	protected void runImpl() {
		GameServerInfo gsi = getConnection().getGameServerInfo();
		if (gsi == null) // the link was closed while this packet waited, its accounts are gone already
			return;
		Account account = gsi.removeAccountFromGameServer(accountId);

		/**
		 * account can be null if a player logged out from gs {@link CM_ACCOUNT_RECONNECT_KEY}
		 */
		if (account != null) {
			AccountTimeController.updateOnLogout(account);
		}
	}
}
