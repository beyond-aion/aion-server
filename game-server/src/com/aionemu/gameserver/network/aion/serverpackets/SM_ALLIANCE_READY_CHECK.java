package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sarynth (Thx Rhys2002 for Packets)
 */
public class SM_ALLIANCE_READY_CHECK extends AionServerPacket {

	private int playerObjectId;
	private int statusCode;

	public SM_ALLIANCE_READY_CHECK(int playerObjectId, int statusCode) {
		this.playerObjectId = playerObjectId;
		this.statusCode = statusCode;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerObjectId);
		writeC(statusCode);
	}

}
