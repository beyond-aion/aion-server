package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * This packet is sent alongside SM_PLAYER_STATE to the protected player on retail.<br>
 * It does not have any effect on the client. SM_PLAYER_STATE is what controls the blinking.
 * 
 * @author SVDNESS
 */
public class SM_INVINCIBLE_TIME extends AionServerPacket {

	private final int timeMs;

	public SM_INVINCIBLE_TIME(int timeMs) {
		this.timeMs = timeMs;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(timeMs);
	}
}