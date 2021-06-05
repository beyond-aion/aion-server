package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Neon
 */
public class SM_CLOSE_QUESTION_WINDOW extends AionServerPacket {

	/**
	 * %0 has withdrawn the challenge for a duel.
	 */
	public static SM_CLOSE_QUESTION_WINDOW STR_DUEL_REQUESTER_WITHDRAW_REQUEST(String value0) {
		return new SM_CLOSE_QUESTION_WINDOW(1300134, value0);
	}

	/**
	 * %0 declined your challenge.
	 */
	public static SM_CLOSE_QUESTION_WINDOW STR_DUEL_HE_REJECT_DUEL(String value0) {
		return new SM_CLOSE_QUESTION_WINDOW(1300097, value0);
	}

	public static SM_CLOSE_QUESTION_WINDOW CLOSE_QUESTION_WINDOW() {
		return new SM_CLOSE_QUESTION_WINDOW(0);
	}

	private static final int MAX_PARAM_COUNT = 3;

	private final int msgId;
	private final Object[] params;

	public SM_CLOSE_QUESTION_WINDOW(int msgId, Object... params) {
		this.msgId = msgId;
		this.params = params;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(0); // maybe a target object id?
		writeD(msgId); // reason
		for (int i = 0; i < MAX_PARAM_COUNT; i++) // client only supports three parameters in this package (fourth will not be rendered)
			writeS(i < params.length ? String.valueOf(params[i]) : null);
		// unknown what follows here
	}
}
