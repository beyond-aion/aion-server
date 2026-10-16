package com.aionemu.gameserver.model.gameobjects.player.motion;

/**
 * The slot a custom animation replaces, a player can have one active animation per slot.
 */
public enum MotionType {

	IDLE(1),
	RUN(2),
	JUMP(3),
	REST(4),
	SHOP(5);

	private final int id;

	MotionType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	/**
	 * @return The type with the given id, or null if there's none.
	 */
	public static MotionType getById(int id) {
		for (MotionType type : values()) {
			if (type.id == id)
				return type;
		}
		return null;
	}
}
