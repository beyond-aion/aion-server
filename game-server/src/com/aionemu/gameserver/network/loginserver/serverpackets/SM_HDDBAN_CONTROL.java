package com.aionemu.gameserver.network.loginserver.serverpackets;

import com.aionemu.gameserver.network.loginserver.LoginServerConnection;
import com.aionemu.gameserver.network.loginserver.LsServerPacket;
import com.aionemu.gameserver.services.ban.BanAction;

/**
 * @author ViAl
 */
public class SM_HDDBAN_CONTROL extends LsServerPacket {

	private BanAction action;
	private String serial;
	private long time;

	public SM_HDDBAN_CONTROL(BanAction action, String address, long time) {
		super(14);
		this.action = action;
		this.serial = address;
		this.time = time;
	}

	@Override
	protected void writeImpl(LoginServerConnection con) {
		writeC(action.getId());
		writeS(serial);
		writeQ(time);
	}

}
