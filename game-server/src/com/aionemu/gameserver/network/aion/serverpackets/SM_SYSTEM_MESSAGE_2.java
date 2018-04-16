package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Similar to {@link SM_SYSTEM_MESSAGE} and {@link SM_QUESTION_WINDOW} , maybe there's some undiscovered feature / missing bytes or flags
 * 
 * @author Neon
 */
public class SM_SYSTEM_MESSAGE_2 extends AionServerPacket {

	private static final int MAX_PARAM_COUNT = 3;

	private final int msgId;
	private final Object[] params;

	public SM_SYSTEM_MESSAGE_2(int msgId, Object... params) {
		this.msgId = msgId;
		this.params = params;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(0); // maybe a target object id?
		writeD(msgId);
		for (int i = 0; i < MAX_PARAM_COUNT; i++) // client only supports three parameters in this package (fourth will not be rendered)
			writeS(i < params.length ? String.valueOf(params[i]) : null);
		// unknown what follows here
	}
}
