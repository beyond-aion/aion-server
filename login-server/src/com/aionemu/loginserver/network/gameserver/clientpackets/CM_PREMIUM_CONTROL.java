package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.controller.PremiumController;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;

/**
 * @author KID
 */
public class CM_PREMIUM_CONTROL extends GsClientPacket {

	private int accountId;
	private int requestId;
	private long requiredCost;
	private byte serverId;

	@Override
	protected void readImpl() {
		accountId = readD();
		requestId = readD();
		requiredCost = readQ();
		serverId = (byte)readC();
	}
	
	@Override
	protected void runImpl() {
		PremiumController.getController().requestBuy(accountId, requestId, requiredCost, serverId);
	}
}
