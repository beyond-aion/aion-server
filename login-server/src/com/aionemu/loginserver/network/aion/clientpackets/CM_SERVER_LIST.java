package com.aionemu.loginserver.network.aion.clientpackets;

import java.nio.ByteBuffer;

import com.aionemu.loginserver.GameServerTable;
import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.network.aion.AionAuthResponse;
import com.aionemu.loginserver.network.aion.AionClientPacket;
import com.aionemu.loginserver.network.aion.LoginConnection;
import com.aionemu.loginserver.network.aion.serverpackets.SM_LOGIN_FAIL;

/**
 * @author -Nemesiss-
 */
public class CM_SERVER_LIST extends AionClientPacket {

	/**
	 * accountId is part of session key - its used for security purposes
	 */
	private int accountId;
	/**
	 * loginOk is part of session key - its used for security purposes
	 */
	private int loginOk;

	public CM_SERVER_LIST(ByteBuffer buf, LoginConnection client, int opCode) {
		super(buf, client, opCode);
	}

	@Override
	protected void readImpl() {
		accountId = readD();
		loginOk = readD();
		readC(); // always 7
		readB(6); // static per session when coming from char selection, random otherwise
		readD(); // always random
		readD(); // 60222 when coming from char selection, random otherwise
	}

	@Override
	protected void runImpl() {
		LoginConnection con = getConnection();
		if (con.getSessionKey().checkLogin(accountId, loginOk)) {
			if (GameServerTable.size() == 0)
				con.close(new SM_LOGIN_FAIL(AionAuthResponse.STR_L2AUTH_S_NO_SERVER_LIST));
			else
				AccountController.loadGSCharactersCount(accountId);
		} else {
			con.close(new SM_LOGIN_FAIL(AionAuthResponse.STR_L2AUTH_S_SYSTEM_ERROR));
		}
	}
}
