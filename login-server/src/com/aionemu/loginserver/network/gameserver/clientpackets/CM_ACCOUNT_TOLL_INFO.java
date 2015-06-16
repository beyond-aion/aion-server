package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.loginserver.dao.AccountDAO;
import com.aionemu.loginserver.dao.PremiumDAO;
import com.aionemu.loginserver.model.Account;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;

/**
 * @author xTz
 */
public class CM_ACCOUNT_TOLL_INFO extends GsClientPacket {

	private long toll;

	private String accountName;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void readImpl() {
		toll = readQ();
		accountName = readS();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void runImpl() {
		Account account = DAOManager.getDAO(AccountDAO.class).getAccount(accountName);

		if (account != null)
			DAOManager.getDAO(PremiumDAO.class).updatePoints(account.getId(), toll, 0);
	}
}
