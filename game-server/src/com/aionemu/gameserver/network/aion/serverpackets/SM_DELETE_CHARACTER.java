package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * In this packet Server is sending response for CM_DELETE_CHARACTER.
 * 
 * @author -Nemesiss-
 */
public class SM_DELETE_CHARACTER extends AionServerPacket {

	private int playerObjId;
	private int deletionTime;

	/**
	 * Constructs new <tt>SM_DELETE_CHARACTER </tt> packet
	 */
	public SM_DELETE_CHARACTER(int playerObjId, int deletionTime) {
		this.playerObjId = playerObjId;
		this.deletionTime = deletionTime;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		if (playerObjId != 0) {
			writeD(0x00);// unk
			writeD(playerObjId);
			writeD(deletionTime);
		} else {
			writeD(0x10);// unk
			writeD(0x00);
			writeD(0x00);
		}
	}
}
