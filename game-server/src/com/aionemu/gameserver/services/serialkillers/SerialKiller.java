package com.aionemu.gameserver.services.serialkillers;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Source
 */
public class SerialKiller {

	private final int playerId;
	private int killerRank;
	private int ldRank;
	private int victims;
	private SerialKillerDebuff buff;

	public SerialKiller(Player owner) {
		playerId = owner.getObjectId();
		buff = new SerialKillerDebuff();
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setRank(int rank) {
		killerRank = rank;
	}

	public void setLDRank(int rank) {
		ldRank = rank;
	}

	public int getRank() {
		return killerRank;
	}

	public int getLDRank() {
		return ldRank;
	}

	public int getVictims() {
		return victims;
	}

	public void setVictims(int victims) {
		this.victims = victims;
	}

	public SerialKillerDebuff getBuff() {
		return buff;
	}

}
