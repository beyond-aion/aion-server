package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * dunno wtf this packet is doing. Not sure about id/name
 * 
 * @author -Nemesiss-
 */
public class SM_ENTER_WORLD_CHECK extends AionServerPacket {

	private byte msg = 0x00;

	public SM_ENTER_WORLD_CHECK(byte msg) {
		this.msg = msg;
	}

	public SM_ENTER_WORLD_CHECK() {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		writeC(msg);
		writeC(0x00);
		writeC(0x00);
	}
}
