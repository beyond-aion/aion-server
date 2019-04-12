package com.aionemu.gameserver.services.conquerorAndProtectorSystem;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author Source
 */
public class CPInfo {

	private final int playerId;
	private int rank;
	private int ldRank;
	private int victims;
	private CPBuff buff;

	public CPInfo(Player owner) {
		playerId = owner.getObjectId();
		buff = new CPBuff();
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public void setLDRank(int rank) {
		ldRank = rank;
	}

	public int getRank() {
		return rank;
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

	public CPBuff getBuff() {
		return buff;
	}

}
