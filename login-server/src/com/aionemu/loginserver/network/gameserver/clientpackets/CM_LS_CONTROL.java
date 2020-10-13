package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.loginserver.dao.AccountDAO;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.serverpackets.SM_LS_CONTROL_RESPONSE;

/**
 * @author Aionchs-Wylovech
 */
public class CM_LS_CONTROL extends GsClientPacket {

	private byte type, param;
	private int accountId, adminId;

	@Override
	protected void readImpl() {
		type = readC();
		param = readC();
		accountId = readD();
		adminId = readD();
	}

	@Override
	protected void runImpl() {
		Account account = DAOManager.getDAO(AccountDAO.class).getAccount(accountId);
		switch (type) {
			case 1:
				account.setAccessLevel(param);
				break;
			case 2:
				account.setMembership(param);
				break;
		}
		boolean result = DAOManager.getDAO(AccountDAO.class).updateAccount(account);
		sendPacket(new SM_LS_CONTROL_RESPONSE(type, param, account.getId(), adminId, result));
	}
}
