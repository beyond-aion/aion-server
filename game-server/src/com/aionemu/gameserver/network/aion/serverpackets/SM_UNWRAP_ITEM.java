package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xTz
 */
public class SM_UNWRAP_ITEM extends AionServerPacket {

	private final int objectId, count;

	public SM_UNWRAP_ITEM(int objectId, int count) {
		this.objectId = objectId;
		this.count = count;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(objectId);
		writeC(count);
	}

}
