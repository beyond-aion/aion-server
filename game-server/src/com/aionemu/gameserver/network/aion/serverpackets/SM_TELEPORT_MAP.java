package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author alexa026 , orz
 */
public class SM_TELEPORT_MAP extends AionServerPacket {

	private int targetObjId;
	private int teleportId;

	public SM_TELEPORT_MAP(int targetObjId, int teleportId) {
		this.targetObjId = targetObjId;
		this.teleportId = teleportId;
		
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetObjId);
		writeH(teleportId);
	}
}
