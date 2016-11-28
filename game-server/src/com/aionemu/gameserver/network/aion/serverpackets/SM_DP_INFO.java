package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sweetkr
 */
public class SM_DP_INFO extends AionServerPacket {

	private int playerObjectId;
	private int currentDp;

	public SM_DP_INFO(int playerObjectId, int currentDp) {
		this.playerObjectId = playerObjectId;
		this.currentDp = currentDp;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerObjectId);
		writeH(currentDp);
	}

}
