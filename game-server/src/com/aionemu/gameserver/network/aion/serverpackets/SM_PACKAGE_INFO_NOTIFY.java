package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas
 */
public class SM_PACKAGE_INFO_NOTIFY extends AionServerPacket {

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(1);
		writeC(3);
		writeD(0); // time until pack expiration
	}

}
