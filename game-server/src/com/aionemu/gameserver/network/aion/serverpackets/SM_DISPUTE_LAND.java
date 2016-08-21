package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

import javolution.util.FastTable;

/**
 * @author Source
 */
public class SM_DISPUTE_LAND extends AionServerPacket {

	FastTable<Integer> worlds;
	boolean active;

	public SM_DISPUTE_LAND(FastTable<Integer> worlds, boolean active) {
		this.worlds = worlds;
		this.active = active;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(worlds.size());
		for (int world : worlds) {
			writeD(active ? 0x02 : 0x01);
			writeD(world);
			writeQ(0x00);
			writeQ(0x00);
			writeQ(0x00);
			writeQ(0x00);
		}
	}

}
