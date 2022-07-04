package com.aionemu.gameserver.custom.instance;

/**
 * @author Estrayl
 */
public class CustomInstanceRank {

	private final int playerId;
	private int rank;

	private int maxRank;
	private int dps;
	private long lastEntry;

	public CustomInstanceRank(int playerId, int rank, long lastEntry, int maxRank, int dps) {
		this.playerId = playerId;
		this.rank = rank;
		this.lastEntry = lastEntry;
		this.maxRank = maxRank;
		this.dps = dps;
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

	public int getMaxRank() {
		return maxRank;
	}

	public void setMaxRank(int maxRank) {
		this.maxRank = maxRank;
	}

	public int getDps() {
		return dps;
	}

	public void setDps(int dps) {
		this.dps = dps;
	}
}
