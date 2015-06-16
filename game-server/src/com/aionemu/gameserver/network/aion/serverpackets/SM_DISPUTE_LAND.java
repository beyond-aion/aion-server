package com.aionemu.gameserver.network.aion.serverpackets;

import javolution.util.FastList;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Source
 */
public class SM_DISPUTE_LAND extends AionServerPacket {

	FastList<Integer> worlds;
	boolean active;

	public SM_DISPUTE_LAND(FastList<Integer> worlds, boolean active) {
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