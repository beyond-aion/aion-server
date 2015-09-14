package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Simple
 */
public class SM_LOGIN_QUEUE extends AionServerPacket {

	private int waitingPosition; // What is the player's position in line
	private int waitingTime; // Per waiting position in seconds
	private int waitingCount; // How many are waiting in line

	private SM_LOGIN_QUEUE() {
		this.waitingPosition = 5;
		this.waitingTime = 60;
		this.waitingCount = 50;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(waitingPosition);
		writeD(waitingTime);
		writeD(waitingCount);
	}
}
