package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.controller.BannedMacManager;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.GsConnection;

import io.netty.buffer.ByteBuf;

/**
 * @author KID
 */
public class CM_MACBAN_CONTROL extends GsClientPacket {

	private byte type;
	private String address;
	private String details;
	private long time;

	public CM_MACBAN_CONTROL(ByteBuf buf, GsConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		type = readC();
		address = readS();
		details = readS();
		time = readQ();
	}

	@Override
	protected void runImpl() {
		BannedMacManager bmm = BannedMacManager.getInstance();
		switch (type) {
			case 0:// unban
				bmm.unban(address, details);
				break;
			case 1:// ban
				bmm.ban(address, time, details);
				break;
		}
	}
}
