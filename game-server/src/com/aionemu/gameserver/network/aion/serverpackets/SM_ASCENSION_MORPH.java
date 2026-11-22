package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Switches the ascension morph of the intro and ascension quest instances on or off. While it is on, the client shows the player in the
 * polymorph equipment of their armor type ("Tunic Form", "Breastplate Form" and so on) whenever it loads their equipment.
 * 
 * @author wylovech
 */
public class SM_ASCENSION_MORPH extends AionServerPacket {

	private final boolean active;

	public SM_ASCENSION_MORPH(boolean active) {
		this.active = active;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(active ? 1 : 0);
	}
}
