package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.dao.AccountDAO;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;

public class CM_CHANGE_ALLOWED_HDD_SERIAL extends GsClientPacket {

	private int accountId;
	private String hddSerial;

	protected void readImpl() {
		accountId = readD();
		hddSerial = readS();
	}

	protected void runImpl() {
		AccountDAO.updateAllowedHDDSerial(accountId, hddSerial);
	}
}
