package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.GameServerInfo;
import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.GsConnection;

import io.netty.buffer.ByteBuf;

/**
 * @author cura
 */
public class CM_GS_CHARACTER extends GsClientPacket {

	private int accountId;
	private int characterCount;

	public CM_GS_CHARACTER(ByteBuf buf, GsConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

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
