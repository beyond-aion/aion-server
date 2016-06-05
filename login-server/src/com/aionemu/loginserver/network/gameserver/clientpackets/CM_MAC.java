package com.aionemu.loginserver.network.gameserver.clientpackets;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.loginserver.dao.AccountDAO;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;

/**
 * @author nrg
 */
public class CM_MAC extends GsClientPacket {

	private int accountId;
	private String address;

	@Override
	protected void readImpl() {
		accountId = readD();
		address = readS();
	}

	@Override
	protected void runImpl() {
		if (!DAOManager.getDAO(AccountDAO.class).updateLastMac(accountId, address))
			LoggerFactory.getLogger(CM_MAC.class).warn("Couldn't update account_data.last_mac for accountId " + accountId);
	}
}
