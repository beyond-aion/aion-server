package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Yeats
 *
 */
public class SM_STONESPEAR_RANKING extends AionServerPacket {

	private int id;
	
	public SM_STONESPEAR_RANKING(int id) {
		this.id = id;
	}
	
	@Override
	protected void writeImpl(AionConnection con) {
		writeD(id); //id
		writeC(0); //current rank position of this legion
		writeC(0); //amount of legions in ranking
		writeC(0); //unknown 0x00
		//if participated: 
		/* TODO
		for (int i = 0; i < size; i++) {
			writeD(0); //points reached
			writeD(0); //time needed in seconds
			writeQ(0); //participation time
			writeS(""); //legion name
		} */
	}
}
