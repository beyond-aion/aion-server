package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas
 */
public class SM_STATS_STATUS_UNK extends AionServerPacket {

	int lvl;
	int points;

	public SM_STATS_STATUS_UNK(int lvl, int points) {
		this.lvl = lvl;
		this.points = points;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(points);
		writeC(1);
		if (lvl == 50)
			writeC(1);
		else
			writeC(2);
		writeD(lvl);
		writeD(lvl);
		writeD(lvl == 50 ? 1 : 0);
		writeC(0);
	}
}
