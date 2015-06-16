package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author pixfid, Rolandas
 */
public class SM_ACCOUNT_PROPERTIES extends AionServerPacket {
	
	private boolean isGM;
	
	public SM_ACCOUNT_PROPERTIES(boolean isGM) {
		this.isGM = isGM;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		if (isGM) {
			writeH(1);
			writeH(0x00);
			writeD(0x00);
			writeD(9);
			writeD(0x00);
			writeD(0x00);
			writeC(0x00);
			writeD(0x08);
			writeD(0x04);
		}
		else {
			writeH(0x00);
			writeH(0x00);
			writeD(0x00);
			writeD(9);
			writeD(0x00);
			writeD(0x00);
			writeC(0x00);
			writeD(0x00);
			writeD(0x00);
		}
	}
}
