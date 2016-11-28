package com.aionemu.gameserver.network.aion.serverpackets;

import java.sql.Timestamp;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * I have no idea wtf is this
 * 
 * @author -Nemesiss-
 */
public class SM_TIME_CHECK extends AionServerPacket {

	// Don't be fooled with empty class :D
	// This packet is just sending opcode, without any content

	// 1.5.x sending 8 bytes

	private int nanoTime;
	private int time;
	private Timestamp dateTime;

	public SM_TIME_CHECK(int nanoTime) {
		this.dateTime = new Timestamp((new java.util.Date()).getTime());
		this.nanoTime = nanoTime;
		this.time = (int) dateTime.getTime();
	}


	@Override
	protected void writeImpl(AionConnection con) {
		writeD(time);
		writeD(nanoTime);

	}
}
