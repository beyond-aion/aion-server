package com.aionemu.chatserver.model;

/**
 * @author ATracer
 */
public enum Race {
	ELYOS(0),
	ASMODIANS(1);

	/**
	 * id of race
	 */
	private int raceId;

	/**
	 * @param raceId
	 */
	private Race(int raceId) {
		this.raceId = raceId;
	}

	/**
	 * @return the raceId
	 */
	public int getRaceId() {
		return raceId;
	}
}
