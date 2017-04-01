package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Notifies players when their friends log in, out, or delete them
 * 
 * @author Ben
 */
public class SM_FRIEND_NOTIFY extends AionServerPacket {

	/**
	 * Buddy has logged in (Or become visible)
	 */
	public static final byte LOGIN = 0;
	/**
	 * Buddy has logged out (Or become invisible)
	 */
	public static final byte LOGOUT = 1;
	/**
	 * Buddy has deleted you
	 */
	public static final byte DELETED = 2;

	private final byte code;
	private final String name;

	/**
	 * Constructs a new notify packet
	 * 
	 * @param code
	 *          Message code
	 * @param name
	 *          Name of friend
	 */
	public SM_FRIEND_NOTIFY(byte code, String name) {
		this.code = code;
		this.name = name;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeS(name);
		writeC(code);
	}
}
