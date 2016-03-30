package com.aionemu.gameserver.model.gameobjects.player;

/**
 * @author Rolandas
 */
public enum HouseOwnerState {
	HAS_OWNER(1 << 0),
	BUY_STUDIO_ALLOWED(1 << 1),
	SINGLE_HOUSE(1 << 1),
	BIDDING_ALLOWED(1 << 2),

	HOUSE_OWNER((HAS_OWNER.getId() | BIDDING_ALLOWED.getId()) & ~BUY_STUDIO_ALLOWED.getId()),
	SELLING_HOUSE((HAS_OWNER.getId() | BIDDING_ALLOWED.getId()) & ~BUY_STUDIO_ALLOWED.getId()); // identical? remove?

	private byte id;

	private HouseOwnerState(int id) {
		this.id = (byte) (id & 0xFF);
	}

	public byte getId() {
		return id;
	}
}
