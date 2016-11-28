package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Response for CM_RECONNECT_AUTH with key that will be use for authentication at LoginServer.
 * 
 * @author -Nemesiss-
 */
public class SM_RECONNECT_KEY extends AionServerPacket {

	/**
	 * key for reconnection - will be used for authentication
	 */
	private final int key;

	/**
	 * Constructs new <tt>SM_RECONNECT_KEY</tt> packet
	 * 
	 * @param key
	 *          key for reconnection
	 */
	public SM_RECONNECT_KEY(int key) {
		this.key = key;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(0x00);
		writeD(key);
	}
}
