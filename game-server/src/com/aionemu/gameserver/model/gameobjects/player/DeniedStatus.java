package com.aionemu.gameserver.model.gameobjects.player;

/**
 * @author Sweetkr
 */
public enum DeniedStatus {
	VIEW_DETAILS(1),
	TRADE(2),
	GROUP(4),
	GUILD(8),
	FRIEND(16),
	DUEL(32);

	private int id;

	private DeniedStatus(int id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
}
