package com.aionemu.gameserver.model.items;

/**
 * @author ATracer
 */
public enum ItemId {
	KINAH(182400001);

	private int itemId;

	private ItemId(int itemId) {
		this.itemId = itemId;
	}

	public int value() {
		return itemId;
	}
}
