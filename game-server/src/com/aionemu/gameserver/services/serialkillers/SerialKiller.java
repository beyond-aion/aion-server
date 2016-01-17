package com.aionemu.gameserver.services.serialkillers;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Source
 */
public class SerialKiller {

	private Player owner;
	private int killerRank;
	private int ldRank;
	public int victims;
	private SerialKillerDebuff buff;

	public SerialKiller(Player owner) {
		this.owner = owner;
		buff = new SerialKillerDebuff();
	}

	public void refreshOwner(Player player) {
		owner = player;
	}

	public Player getOwner() {
		return owner;
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
	
	public SerialKillerDebuff getBuff() {
		return buff;
	}

}
