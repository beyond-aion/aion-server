package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Avol
 */
public class SM_EXCHANGE_ADD_KINAH extends AionServerPacket {

	private long kinahCount;
	private int action;

	public SM_EXCHANGE_ADD_KINAH(long kinahCount, int action) {
		this.kinahCount = kinahCount;
		this.action = action;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action); // 0 -self 1-other
		writeQ(kinahCount);
	}
}
