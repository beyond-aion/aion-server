package com.aionemu.gameserver.model;

/**
 * @author zdead
 */
public class RankCount {

	private int playerId;
	private int ap;
	private Race race;

	public RankCount(int playerId, int ap, Race race) {
		this.playerId = playerId;
		this.ap = ap;
		this.race = race;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getPlayerAP() {
		return ap;
	}

	public Race getPlayerRace() {
		return race;
	}
}
