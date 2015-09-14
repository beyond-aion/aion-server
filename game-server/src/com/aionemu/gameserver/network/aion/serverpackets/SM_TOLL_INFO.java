package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xTz
 */
public class SM_TOLL_INFO extends AionServerPacket {

	private long tollCount;

	public SM_TOLL_INFO(long tollCount) {
		this.tollCount = tollCount;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeQ(tollCount);
	}
}
