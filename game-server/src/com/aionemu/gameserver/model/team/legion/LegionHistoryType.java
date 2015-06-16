package com.aionemu.gameserver.model.team.legion;

/**
 * @author Simple
 */
public enum LegionHistoryType {
	CREATE(0), // No parameters
	JOIN(1), // Parameter: name
	KICK(2), // Parameter: name
	LEVEL_UP(3), // Parameter: legion level
	APPOINTED(4), // Parameter: legion level
	EMBLEM_REGISTER(5), // No parameters
	EMBLEM_MODIFIED(6), // No parameters
	ITEM_DEPOSIT(15), // Parameter: name
	ITEM_WITHDRAW(16), // Parameter: name
	KINAH_DEPOSIT(17), // Parameter: name
	KINAH_WITHDRAW(18); // Parameter: name

	private byte historyType;

	private LegionHistoryType(int historyType) {
		this.historyType = (byte) historyType;
	}

	/**
	 * Returns client-side id for this
	 * 
	 * @return byte
	 */
	public byte getHistoryId() {
		return this.historyType;
	}
}
