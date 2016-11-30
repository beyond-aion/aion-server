package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.controller.BannedHDDController;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;

/**
 * @author ViAl
 */
public class CM_HDDBAN_CONTROL extends GsClientPacket {

	private byte type;
	private String address;
	private long time;

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
