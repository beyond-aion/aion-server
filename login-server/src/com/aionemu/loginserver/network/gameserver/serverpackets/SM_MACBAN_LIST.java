package com.aionemu.loginserver.network.gameserver.serverpackets;

import java.util.List;

import com.aionemu.loginserver.controller.BannedMacManager;
import com.aionemu.loginserver.model.base.BannedMacEntry;
import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsServerPacket;

/**
 * @author KID
 */
public class SM_MACBAN_LIST extends GsServerPacket {

	private final List<BannedMacEntry> bannedList;

	public SM_MACBAN_LIST() {
		bannedList = List.copyOf(BannedMacManager.getInstance().getMap().values()); // the count must match the entries written on the I/O thread
	}

	@Override
	protected void writeImpl(GsConnection con) {
		writeC(9);
		writeD(bannedList.size());

		for (BannedMacEntry entry : bannedList) {
			writeS(entry.getMac());
			writeQ(entry.getTime().getTime());
			writeS(entry.getDetails());
		}
	}
}
