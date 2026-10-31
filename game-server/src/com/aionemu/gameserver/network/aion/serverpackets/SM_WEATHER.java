package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer, Kwazar, Nemesiss
 */
public class SM_WEATHER extends AionServerPacket {

	private final int[] codes;

	/**
	 * @param codes Index 0 is the weather of the whole map, indexes 1..n are the weather zones 1..n.
	 */
	public SM_WEATHER(int[] codes) {
		this.codes = codes;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(codes[0]); // weather of the whole map
		writeC(codes.length - 1);
		for (int zoneId = 1; zoneId < codes.length; zoneId++)
			writeC(codes[zoneId]);
	}
}
