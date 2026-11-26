package com.aionemu.loginserver.network.gameserver.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.GameServerInfo;
import com.aionemu.loginserver.configs.Config;
import com.aionemu.loginserver.dao.AccountDAO;
import com.aionemu.loginserver.dao.AccountsLogDAO;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.GsConnection;

import io.netty.buffer.ByteBuf;

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

	public CM_ACCOUNT_CONNECTION_INFO(ByteBuf buf, GsConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	protected void readImpl() {
		accountId = readD();
		time = readQ();
		ip = readS();
		mac = readS();
		hddSerial = readS();
	}

	protected void runImpl() {
		if (!AccountDAO.updateLastMac(accountId, mac))
			log.warn("Couldn't update account_data.last_mac for accountId {}", accountId);
		if (!AccountDAO.updateLastHDDSerial(accountId, hddSerial))
			log.warn("Couldn't update account_data.last_hdd_serial for accountId {}", accountId);
		GameServerInfo gsi = getConnection().getGameServerInfo();
		if (Config.LOG_LOGINS && gsi != null) // null if the link was closed while this packet waited
			AccountsLogDAO.addRecord(accountId, gsi.getId(), time, ip, mac, hddSerial);
	}
}
