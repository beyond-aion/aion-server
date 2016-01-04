package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

public class SM_GAMEGUARD extends AionServerPacket {

	private int size;

	public SM_GAMEGUARD(int size) {
		this.size = size;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(size);
		writeB(new byte[size]);
	}
}
