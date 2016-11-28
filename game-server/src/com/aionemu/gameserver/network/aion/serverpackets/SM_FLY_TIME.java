package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Nemiroff
 */
public class SM_FLY_TIME extends AionServerPacket {

	private int currentFp;
	private int maxFp;

	public SM_FLY_TIME(int currentFp, int maxFp) {
		this.currentFp = currentFp;
		this.maxFp = maxFp;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(currentFp); // current fly time
		writeD(maxFp); // max flytime
	}
}
