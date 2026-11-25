package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.dao.AccountDAO;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.GsConnection;

import io.netty.buffer.ByteBuf;

public class CM_CHANGE_ALLOWED_HDD_SERIAL extends GsClientPacket {

	private int accountId;
	private String hddSerial;

	public CM_CHANGE_ALLOWED_HDD_SERIAL(ByteBuf buf, GsConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	protected void readImpl() {
		accountId = readD();
		hddSerial = readS();
	}

	protected void runImpl() {
		AccountDAO.updateAllowedHDDSerial(accountId, hddSerial);
	}
}
