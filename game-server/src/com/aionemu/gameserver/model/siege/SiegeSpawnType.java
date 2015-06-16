package com.aionemu.gameserver.model.siege;

public enum SiegeSpawnType {
	PEACE(0),
	GUARD(1),
	ARTIFACT(2),
	PROTECTOR(3),
	MINE(4),
	PORTAL(5),
	GENERATOR(6),
	SPRING(7),
	RACEPROTECTOR(8);

	private int id;

	private SiegeSpawnType(int id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
}
