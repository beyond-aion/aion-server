package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * This packet is response for CM_MAY_LOGIN_INTO_GAME
 * 
 * @author -Nemesiss-
 */
public class SM_MAY_LOGIN_INTO_GAME extends AionServerPacket {

	@Override
	protected void writeImpl(AionConnection con) {
		/**
		 * probably here is msg if fail.
		 */
		writeD(0x00);
	}
}
