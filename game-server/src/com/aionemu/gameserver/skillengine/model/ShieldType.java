package com.aionemu.gameserver.skillengine.model;

/**
 * @author kecimis, Neon
 */
public enum ShieldType {
	CONVERT(0),
	REFLECTOR(1 << 0), // 1
	NORMAL(1 << 1), // 2
	UNK(1 << 2), // 4
	PROTECT(1 << 3), // 8
	MPSHIELD(1 << 4), // 16
	SKILL_REFLECTOR(1 << 5); // 32

	private int id;

	private ShieldType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}
}
