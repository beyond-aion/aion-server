package com.aionemu.loginserver.network.gameserver.clientpackets;

import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;


/**
 * @author ViAl
 *
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
		if(!AccountController.refreshAccountsLastHDDSerial(accountId, hddSerial))
				LoggerFactory.getLogger(CM_HDD_SERIAL.class).error("[WARN] We just weren't able to update account_data.last_hdd_serial for accountId "+accountId);
	}
}
