package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author alexa026
 */
public class SM_LOOT_STATUS extends AionServerPacket {

	private int targetObjectId;
	private int state;

	public SM_LOOT_STATUS(int targetObjectId, int state) {
		this.targetObjectId = targetObjectId;
		this.state = state;
	}

	/**
	 * {@inheritDoc} dc
	 */

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetObjectId);
		writeC(state);
	}
}
