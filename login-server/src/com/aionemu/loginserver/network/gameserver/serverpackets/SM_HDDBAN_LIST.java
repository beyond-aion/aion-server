package com.aionemu.loginserver.network.gameserver.serverpackets;

import java.sql.Timestamp;
import java.util.Map;
import java.util.Map.Entry;

import com.aionemu.loginserver.controller.BannedHDDController;
import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsServerPacket;

/**
 * @author ViAl
 */
public class SM_HDDBAN_LIST extends GsServerPacket {

	private Map<String, Timestamp> bannedList;

	public SM_HDDBAN_LIST() {
		this.bannedList = BannedHDDController.getInstance().getMap();
	}

	@Override
	protected void writeImpl(GsConnection con) {
		writeC(13);
		writeD(bannedList.size());

		for (Entry<String, Timestamp> e : bannedList.entrySet()) {
			writeS(e.getKey());
			writeQ(e.getValue().getTime());
		}
	}

}
