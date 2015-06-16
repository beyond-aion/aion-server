package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas
 */
public class SM_DELETE_HOUSE extends AionServerPacket {

	private int address;

	public SM_DELETE_HOUSE(int address) {
		this.address = address;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(address);
	}
}
