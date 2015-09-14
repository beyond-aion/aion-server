package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author cura
 */
public class SM_LEGION_SEND_EMBLEM_DATA extends AionServerPacket {

	private int size;
	private byte[] data;

	public SM_LEGION_SEND_EMBLEM_DATA(int size, byte[] data) {
		this.size = size;
		this.data = data;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(size);
		writeB(data);
	}
}
