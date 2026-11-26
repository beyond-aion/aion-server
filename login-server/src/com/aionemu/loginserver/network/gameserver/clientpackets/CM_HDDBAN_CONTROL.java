package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.controller.BannedHDDController;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;
import com.aionemu.loginserver.network.gameserver.GsConnection;

import io.netty.buffer.ByteBuf;

/**
 * @author ViAl
 */
public class CM_HDDBAN_CONTROL extends GsClientPacket {

	private byte type;
	private String address;
	private long time;

	public CM_HDDBAN_CONTROL(ByteBuf buf, GsConnection connection, int opCode) {
		super(buf, connection, opCode);
	}

	@Override
	protected void readImpl() {
		type = readC();
		address = readS();
		time = readQ();
	}

	@Override
	protected void runImpl() {
		switch (type) {
			case 0:// unban
				BannedHDDController.getInstance().unban(address);
				break;
			case 1:// ban
				BannedHDDController.getInstance().ban(address, time);
				break;
		}
	}

}
