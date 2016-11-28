package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * This packet is used to update mp / max mp value.
 * 
 * @author Luno
 */
public class SM_STATUPDATE_MP extends AionServerPacket {

	private int currentMp;
	private int maxMp;

	/**
	 * @param currentMp
	 * @param maxMp
	 */
	public SM_STATUPDATE_MP(int currentMp, int maxMp) {
		this.currentMp = currentMp;
		this.maxMp = maxMp;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(currentMp);
		writeD(maxMp);
	}

}
