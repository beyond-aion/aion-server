package com.aionemu.loginserver.network.gameserver.serverpackets;

import com.aionemu.loginserver.network.gameserver.GsConnection;
import com.aionemu.loginserver.network.gameserver.GsServerPacket;

/**
 * @author KID
 */
public class SM_PING extends GsServerPacket {
	@Override
	protected void writeImpl(GsConnection con) {
		writeC(11);
	}
}
