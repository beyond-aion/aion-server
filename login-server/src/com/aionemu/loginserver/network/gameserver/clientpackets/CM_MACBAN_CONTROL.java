package com.aionemu.loginserver.network.gameserver.clientpackets;

import com.aionemu.loginserver.controller.BannedMacManager;
import com.aionemu.loginserver.network.gameserver.GsClientPacket;

/**
 * 
 * @author KID
 *
 */
public class CM_MACBAN_CONTROL  extends GsClientPacket {
	private byte type;
	private String address;
	private String details;
	private long time;

	@Override
	protected void readImpl() {
		type = (byte) readC();
		address = readS();
		details = readS();
		time = readQ();
	}
	
	@Override
	protected void runImpl() {
		BannedMacManager bmm = BannedMacManager.getInstance();
		switch(type)
		{
			case 0://unban
				bmm.unban(address, details);
				break;
			case 1://ban
				bmm.ban(address, time, details);
				break;
		}
	}
}
