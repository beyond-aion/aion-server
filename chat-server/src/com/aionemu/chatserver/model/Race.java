package com.aionemu.chatserver.model;

/**
 * @author ATracer
 */
public enum Race {

	ELYOS(0),
	ASMODIANS(1);

	private final int raceId;

	Race(int raceId) {
		this.raceId = raceId;
	}

	public int getRaceId() {
		return raceId;
	}

	public static Race getById(int id) {
		return id == ELYOS.raceId ? ELYOS : id == ASMODIANS.raceId ? ASMODIANS : null;
	}
}
