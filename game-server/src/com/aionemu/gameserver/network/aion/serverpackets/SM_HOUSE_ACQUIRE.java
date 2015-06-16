package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas
 */
public class SM_HOUSE_ACQUIRE extends AionServerPacket {

	private int playerId;
	private int address;
	private boolean acquire;

	public SM_HOUSE_ACQUIRE(int playerId, int address, boolean acquire) {
		this.playerId = playerId;
		this.address = address;
		this.acquire = acquire;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerId);
		writeD(address);
		writeD(acquire ? 1 : 0); // now it has value 2 sometimes, maybe initial door state ?
	}
}
