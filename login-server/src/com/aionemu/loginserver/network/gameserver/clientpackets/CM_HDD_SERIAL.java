package com.aionemu.loginserver.network.gameserver.clientpackets;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.loginserver.dao.AccountDAO;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;

/**
 * @author ViAl
 */
public class CM_HDD_SERIAL extends GsClientPacket {

	private int accountId;
	private String hddSerial;

	@Override
	protected void readImpl() {
		accountId = readD();
		hddSerial = readS();
	}

	@Override
	protected void runImpl() {
		if (!DAOManager.getDAO(AccountDAO.class).updateLastHDDSerial(accountId, hddSerial))
			LoggerFactory.getLogger(CM_HDD_SERIAL.class).warn("Couldn't update account_data.last_hdd_serial for accountId " + accountId);
	}
}
