package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.dao.AccountsLogDAO;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;

/**
 * @author ViAl
 * @modified Neon
 */
public class CM_ACCOUNT_LOGIN_LOG extends GsClientPacket {

	private int accountId;
	private long time;
	private String ip;
	private String mac;
	private String hddSerial;

	protected void readImpl() {
		accountId = readD();
		time = readQ();
		ip = readS();
		mac = readS();
		hddSerial = readS();
	}

	protected void runImpl() {
		if (Config.LOG_LOGINS)
			DAOManager.getDAO(AccountsLogDAO.class).addRecord(accountId, getConnection().getGameServerInfo().getId(), time, ip, mac, hddSerial);
	}
}
