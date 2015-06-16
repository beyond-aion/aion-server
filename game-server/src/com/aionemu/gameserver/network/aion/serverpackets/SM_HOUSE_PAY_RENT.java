package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas
 */
public class SM_HOUSE_PAY_RENT extends AionServerPacket {

	private int weeksPaid;

	public SM_HOUSE_PAY_RENT(int weeksPaid) {
		this.weeksPaid = weeksPaid;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(0);
		writeC(weeksPaid);
	}

}
