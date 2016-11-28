package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * This packet is used to update current dp (divine points) value.
 * 
 * @author Luno
 */
public class SM_STATUPDATE_DP extends AionServerPacket {

	private int currentDp;

	/**
	 * @param currentDp
	 */
	public SM_STATUPDATE_DP(int currentDp) {
		this.currentDp = currentDp;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(currentDp);
	}

}
