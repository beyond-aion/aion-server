package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

public class SM_FORTRESS_INFO extends AionServerPacket {

	private int locationId;
	private boolean teleportStatus;

	public SM_FORTRESS_INFO(int locationId, boolean teleportStatus) {
		this.locationId = locationId;
		this.teleportStatus = teleportStatus;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(locationId);
		writeC(teleportStatus ? 1 : 0);
	}

}
