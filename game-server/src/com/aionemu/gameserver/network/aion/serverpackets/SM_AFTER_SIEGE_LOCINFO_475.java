package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Ritsu
 */
public class SM_AFTER_SIEGE_LOCINFO_475 extends AionServerPacket {

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(0);
		writeC(0);
	}
}
