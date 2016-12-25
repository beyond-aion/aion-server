package com.aionemu.gameserver.network.loginserver.clientpackets;

import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.network.loginserver.LsClientPacket;
import com.aionemu.gameserver.network.loginserver.serverpackets.SM_LS_PONG;

/**
 * @author KID
 */
public class CM_LS_PING extends LsClientPacket {

	public CM_LS_PING(int opCode) {
		super(opCode);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
		LoginServer.getInstance().sendPacket(new SM_LS_PONG());
	}
}
