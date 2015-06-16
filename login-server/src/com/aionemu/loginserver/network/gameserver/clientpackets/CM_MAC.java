package com.aionemu.loginserver.network.gameserver.clientpackets;

import org.slf4j.LoggerFactory;

import com.aionemu.loginserver.controller.AccountController;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;

/**
 * 
 * @author nrg
 *
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
		if(!AccountController.refreshAccountsLastMac(accountId, address))
				LoggerFactory.getLogger(CM_MAC.class).error("[WARN] We just weren't able to update account_data.last_mac for accountId "+accountId);
	}
}
