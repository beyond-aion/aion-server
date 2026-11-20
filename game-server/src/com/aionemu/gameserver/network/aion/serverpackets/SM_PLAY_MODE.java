package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Switches the client into or out of play mode, which the intro and ascension quest instances use while the player is morphed.
 * 
 * @author wylovech
 */
public class SM_PLAY_MODE extends AionServerPacket {

	private final boolean active;

	public SM_PLAY_MODE(boolean active) {
		this.active = active;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(active ? 1 : 0);
	}
}
