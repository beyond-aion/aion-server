package com.aionemu.gameserver.network.loginserver.clientpackets;

import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.network.loginserver.LsClientPacket;

/**
 * This packet is request kicking player.
 * 
 * @author -Nemesiss-, Neon
 */
public class CM_REQUEST_KICK_ACCOUNT extends LsClientPacket {

	public CM_REQUEST_KICK_ACCOUNT(int opCode) {
		super(opCode);
	}

	/**
	 * account id of account that login server request to kick.
	 */
	private int accountId;
	private boolean notifyDoubleLogin;

	@Override
	public void readImpl() {
		accountId = readD();
		notifyDoubleLogin = readC() == 1;
	}

	@Override
	public void runImpl() {
		LoginServer.getInstance().kickAccount(accountId, notifyDoubleLogin);
	}
}
