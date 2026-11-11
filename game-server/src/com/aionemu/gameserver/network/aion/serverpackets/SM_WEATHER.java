package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer, Kwazar, Nemesiss
 */
public class SM_WEATHER extends AionServerPacket {

	private final int mapWeatherCode;
	private final int[] zoneWeatherCodes;

	/**
	 * @param mapWeatherCode   Weather of the whole map.
	 * @param zoneWeatherCodes Weathers of the weather zones 1..n.
	 */
	public SM_WEATHER(int mapWeatherCode, int[] zoneWeatherCodes) {
		this.mapWeatherCode = mapWeatherCode;
		this.zoneWeatherCodes = zoneWeatherCodes;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(mapWeatherCode);
		writeC(zoneWeatherCodes.length);
		for (int code : zoneWeatherCodes)
			writeC(code);
	}
}
