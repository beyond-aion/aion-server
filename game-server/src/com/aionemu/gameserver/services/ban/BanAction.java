package com.aionemu.gameserver.services.ban;

/**
 * @author ViAl
 */
public enum BanAction {
	UNBAN(0),
	BAN(1);

	private int id;

	private BanAction(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

}
