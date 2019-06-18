package com.aionemu.gameserver.model.gameobjects.player;

/**
 * @author Rolandas
 */
public enum HouseOwnerState {
	HAS_OWNER(1 << 0),
	SINGLE_HOUSE(1 << 1),
	BIDDING_ALLOWED(1 << 2);

	private final byte id;

	HouseOwnerState(int id) {
		this.id = (byte) (id & 0xFF);
	}

	public byte getId() {
		return id;
	}
}
