package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Nemesiss
 *
 */
public class SM_HEADING_UPDATE extends AionServerPacket {
	private final int objectId;
	private final byte heading;

	public SM_HEADING_UPDATE(int objectId, byte heading) {
		this.objectId = objectId;
		this.heading = heading;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(objectId);
		writeC(heading);
	}
}
