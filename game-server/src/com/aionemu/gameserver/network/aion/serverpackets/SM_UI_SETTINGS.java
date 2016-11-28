package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public class SM_UI_SETTINGS extends AionServerPacket {

	private byte[] data;
	private int type;

	/**
	 * Constructs new <tt>SM_CHARACTER_UI </tt> packet
	 */
	public SM_UI_SETTINGS(byte[] data, int type) {
		this.data = data;
		this.type = type;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(type);
		writeH(0x1C00);
		writeB(data);
		if (0x1C00 > data.length)
			writeB(new byte[0x1C00 - data.length]);
	}

}
