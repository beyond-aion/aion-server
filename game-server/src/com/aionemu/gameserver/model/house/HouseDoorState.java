package com.aionemu.gameserver.model.house;

/**
 * @author Neon
 */
public enum HouseDoorState {
	OPEN(1),
	CLOSED_EXCEPT_FRIENDS(2),
	CLOSED(3);

	private final byte id;

	HouseDoorState(int id) {
		this.id = (byte) id;
	}

	public byte getId() {
		return id;
	}

	public static HouseDoorState get(byte id) {
		for (HouseDoorState perm : values()) {
			if (id == perm.id)
				return perm;
		}
		return null;
	}

}
