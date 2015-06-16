package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xTz
 */
public class SM_GROUP_DATA_EXCHANGE extends AionServerPacket {

	private byte[] byteData;
	private int action;
	private int unk2;

	public SM_GROUP_DATA_EXCHANGE(byte[] byteData, int action, int unk2) {
		this.action = action;
		this.byteData = byteData;
		this.unk2 = unk2;
	}

	public SM_GROUP_DATA_EXCHANGE(byte[] byteData) {
		this.action = 1;
		this.byteData = byteData;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action); // action

		if (action != 1)
			writeC(unk2); // unk

		writeD(byteData.length);
		writeB(byteData);
	}
}
