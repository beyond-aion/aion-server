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
		characterCount = readUC();
	}

	@Override
	protected void runImpl() {
		GameServerInfo gsi = getConnection().getGameServerInfo();
		if (gsi == null) // the link was closed while this packet waited
			return;
		AccountController.addGSCharacterCountFor(accountId, gsi.getId(), characterCount);

		if (AccountController.hasAllGSCharacterCounts(accountId))
			AccountController.sendServerListFor(accountId);
	}
}
