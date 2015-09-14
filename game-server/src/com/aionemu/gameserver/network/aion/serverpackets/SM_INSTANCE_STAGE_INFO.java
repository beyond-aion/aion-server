package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xTz
 */
public class SM_INSTANCE_STAGE_INFO extends AionServerPacket {

	private int type;
	private int event;
	private int unk;

	public SM_INSTANCE_STAGE_INFO(int type, int event, int unk) {
		this.type = type;
		this.event = event;
		this.unk = unk;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(type);
		writeD(0);
		writeH(event);
		writeH(unk);
	}
}
