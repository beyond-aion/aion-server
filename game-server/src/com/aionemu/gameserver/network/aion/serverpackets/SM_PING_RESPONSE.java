package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author dragoon112
 */
public class SM_PING_RESPONSE extends AionServerPacket {

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(0x04);
	}
}
