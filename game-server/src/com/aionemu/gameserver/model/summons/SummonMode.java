package com.aionemu.gameserver.model.summons;

/**
 * @author xTz
 */
public enum SummonMode {

	ATTACK(0),
	GUARD(1),
	REST(2),
	RELEASE(3),
	UNK(5);

	private int id;

	private SummonMode(int id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	public static SummonMode getSummonModeById(int id) {
		for (SummonMode mode : values()) {
			if (mode.getId() == id) {
				return mode;
			}
		}
		return null;
	}

}
