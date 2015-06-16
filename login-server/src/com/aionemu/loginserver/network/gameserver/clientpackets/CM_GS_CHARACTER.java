package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.GameServerInfo;
import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;

/**
 * @author cura
 */
public class CM_GS_CHARACTER extends GsClientPacket {

	private int accountId;
	private int characterCount;

	@Override
	protected void readImpl() {
		accountId = readD();
		characterCount = readC();
	}

	@Override
	protected void runImpl() {
		GameServerInfo gsi = this.getConnection().getGameServerInfo();

		AccountController.addGSCharacterCountFor(accountId, gsi.getId(), characterCount);

		if (AccountController.hasAllGSCharacterCounts(accountId))
			AccountController.sendServerListFor(accountId);
	}
}
