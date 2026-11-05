package com.aionemu.gameserver.model;

/**
 * @author xavier
 */
public enum DuelResult {
	DUEL_WON(1300098, (byte) 2),
	DUEL_LOST(1300099, (byte) 0),
	DUEL_DRAW(1300100, (byte) 1);

	private final int msgId;
	private final byte resultCode;

	DuelResult(int msgId, byte resultCode) {
		this.msgId = msgId;
		this.resultCode = resultCode;
	}

	public int getMsgId() {
		return msgId;
	}

	public byte getResultCode() {
		return resultCode;
	}
}
