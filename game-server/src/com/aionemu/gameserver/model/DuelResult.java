package com.aionemu.gameserver.model;

/**
 * @author xavier
 */
public enum DuelResult {
	DUEL_WON(1300098, (byte) 2),
	DUEL_LOST(1300099, (byte) 0),
	DUEL_DRAW(1300100, (byte) 1);

	private int msgId;
	private byte resultId;

	private DuelResult(int msgId, byte resultId) {
		this.msgId = msgId;
		this.resultId = resultId;
	}

	public int getMsgId() {
		return msgId;
	}

	public byte getResultId() {
		return resultId;
	}
}
