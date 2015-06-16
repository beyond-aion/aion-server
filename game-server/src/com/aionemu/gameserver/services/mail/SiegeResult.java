package com.aionemu.gameserver.services.mail;

/**
 * @author Rolandas
 */
public enum SiegeResult {
	DEFENCE(0),
	OCCUPY(1),
	PROTECT(2),
	DEFENDER(3),
	EMPTY(4),
	FAIL(5);

	private int value;

	private SiegeResult(int value) {
		this.value = value;
	}

	public int getId() {
		return this.value;
	}
}
