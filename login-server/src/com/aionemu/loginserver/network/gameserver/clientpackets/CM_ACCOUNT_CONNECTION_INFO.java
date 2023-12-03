package com.aionemu.loginserver.network.gameserver.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.dao.AccountDAO;
import com.aionemu.loginserver.dao.AccountsLogDAO;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;

/**
 * @author ViAl, Neon
 */
public class CM_ACCOUNT_CONNECTION_INFO extends GsClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_ACCOUNT_CONNECTION_INFO.class);
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
		if (!AccountDAO.updateLastMac(accountId, mac))
			log.warn("Couldn't update account_data.last_mac for accountId " + accountId);
		if (!AccountDAO.updateLastHDDSerial(accountId, hddSerial))
			log.warn("Couldn't update account_data.last_hdd_serial for accountId " + accountId);
		if (Config.LOG_LOGINS)
			AccountsLogDAO.addRecord(accountId, getConnection().getGameServerInfo().getId(), time, ip, mac, hddSerial);
	}
}
