package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Yeats
 *
 */
public class SM_STONESPEAR_INFO extends AionServerPacket {

	@Override
	protected void writeImpl(AionConnection con) {
		//TODO
		writeH(6);
		for (int i = 1;  i <= 6; i++) {
			writeD(i); //stonespear Id
			writeD(0); //legionId
			writeC(0); //legion emblem id
			writeC(0); //emblemType
			writeC(0); //alpha
			writeC(0); //r
			writeC(0); //g
			writeC(0); //b
			writeS(""); //legion name
		}
	}
}
