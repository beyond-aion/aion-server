package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.dao.AccountDAO;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_LS_CONTROL_RESPONSE;

import io.netty.buffer.ByteBuf;

/**
 * @author Aionchs-Wylovech
 */
public class CM_LS_CONTROL extends GsClientPacket {

	private byte type, param;
	private int accountId, adminId;

	public CM_LS_CONTROL(ByteBuf buf, GsConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		type = readC();
		param = readC();
		accountId = readD();
		adminId = readD();
	}

	@Override
	protected void runImpl() {
		Account account = AccountDAO.getAccount(accountId);
		switch (type) {
			case 1 -> account.setAccessLevel(param);
			case 2 -> account.setMembership(param);
		}
		boolean result = AccountDAO.updateAccount(account);
		sendPacket(new SM_LS_CONTROL_RESPONSE(type, param, account.getId(), adminId, result));
	}
}
