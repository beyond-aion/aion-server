package com.aionemu.gameserver.model.autogroup;

/**
 *
 * @author xTz
 */
public enum AGQuestion {
	FAILED,
	READY,
	ADDED;

	public boolean isFailed() {
		return this.equals(AGQuestion.FAILED);
	}

	public boolean isReady() {
		return this.equals(AGQuestion.READY);
	}

	public boolean isAdded() {
		return this.equals(AGQuestion.ADDED);
	}
}
