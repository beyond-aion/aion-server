package com.aionemu.gameserver.custom.instance;

/**
 * @author Estrayl
 */
public class CustomInstanceRank {

	private int playerId;
	private int rank;
	private long lastEntry;

	public CustomInstanceRank(int playerId, int rank, long lastEntry) {
		this.playerId = playerId;
		this.rank = rank;
		this.lastEntry = lastEntry;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public long getLastEntry() {
		return lastEntry;
	}

	public void setLastEntry(long lastEntry) {
		this.lastEntry = lastEntry;
	}
}
