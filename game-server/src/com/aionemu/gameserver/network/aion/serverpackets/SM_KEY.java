package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author -Nemesiss-
 */
public class SM_KEY extends AionServerPacket {

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(con.enableCryptKey());
	}
}
