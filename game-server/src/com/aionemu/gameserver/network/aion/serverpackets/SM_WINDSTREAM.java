package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

public class SM_WINDSTREAM extends AionServerPacket {

	private int unk1;
	private int unk2;

	public SM_WINDSTREAM(int unk1, int unk2) {
		this.unk1 = unk1;
		this.unk2 = unk2;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(unk1);
		writeC(unk2);
	}
}
