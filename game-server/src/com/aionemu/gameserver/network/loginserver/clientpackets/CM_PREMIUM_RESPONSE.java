package com.aionemu.gameserver.network.loginserver.clientpackets;

import com.aionemu.gameserver.model.ingameshop.InGameShopEn;
import com.aionemu.gameserver.network.loginserver.LsClientPacket;

/**
 * @author KID
 */
public class CM_PREMIUM_RESPONSE extends LsClientPacket {
	private int requestId;
	private int result;
	private long points;

	public CM_PREMIUM_RESPONSE(int opCode) {
		super(opCode);
	}

	@Override
	protected void readImpl() {
		requestId = readD();
		result = readD();
		points = readQ();
	}

	@Override
	protected void runImpl() {
		InGameShopEn.getInstance().finishRequest(requestId, result, points);
	}
}
