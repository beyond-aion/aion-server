package com.aionemu.gameserver.model.siege;

/**
 * @author Sarynth
 */
public enum SiegeType {
	// Standard
	FORTRESS(0),
	ARTIFACT(1),

	// Balauria Commanders?
	BOSSRAID_LIGHT(2),
	BOSSRAID_DARK(3),

	// Unk
	INDUN(4),
	UNDERPASS(5),
	SOURCE(6);

	private int typeId;

	private SiegeType(int id) {
		this.typeId = id;
	}

	public int getTypeId() {
		return this.typeId;
	}

}
