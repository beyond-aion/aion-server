package com.aionemu.gameserver.model.items;

/**
 * Stage of the item usage animation, as sent in SM_ITEM_USAGE_ANIMATION. Each group is a START / SUCCESS / CANCEL triple, where START carries the cast
 * duration and the other two are sent with a cast time of zero.
 */
public enum ItemUseAnimation {

	USE_START(0),
	USE_SUCCESS(1),
	USE_FAIL(2),
	USE_CANCEL(3),
	SOUL_BIND_START(4),
	SOUL_BIND_SUCCESS(6),
	SOUL_BIND_CANCEL(8),
	IDENTIFY_START(9),
	IDENTIFY_SUCCESS(10),
	IDENTIFY_CANCEL(11),
	REIDENTIFY_START(12),
	REIDENTIFY_SUCCESS(13),
	REIDENTIFY_CANCEL(14);

	private final int id;

	ItemUseAnimation(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
