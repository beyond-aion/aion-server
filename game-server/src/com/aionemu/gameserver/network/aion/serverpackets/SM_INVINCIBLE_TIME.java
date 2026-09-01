package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author SVDNESS
 */
public class SM_INVINCIBLE_TIME extends AionServerPacket {
	private final int timeMs;

	public SM_INVINCIBLE_TIME(int timeMs) {
		this.timeMs = timeMs;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(timeMs);
	}
}