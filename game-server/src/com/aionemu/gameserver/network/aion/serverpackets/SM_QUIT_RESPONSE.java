package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * This packet is response for CM_QUIT
 * 
 * @author -Nemesiss-
 */
public class SM_QUIT_RESPONSE extends AionServerPacket {

	private final int result;
	private final int value;

	public SM_QUIT_RESPONSE() {
		this(false);
	}

	public SM_QUIT_RESPONSE(boolean editMode) {
		this(editMode ? 2 : 1, 0);
	}

	private SM_QUIT_RESPONSE(int result, int value) {
		this.result = result;
		this.value = value;
	}

	/**
	 * @param value
	 *          Seconds left until the player is out of combat, or milliseconds since the quit countdown started if it did not last as long as it should
	 */
	public static SM_QUIT_RESPONSE refused(int value) {
		return new SM_QUIT_RESPONSE(0, value);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(result); // 0 refused, 1 quit, 2 plastic surgery/gender switch
		writeD(value);
		writeD(-1); // home server of a character playing on another server, -1 if it is on its own
	}
}
