package com.aionemu.gameserver.custom.instance;

import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;

/**
 * @author Estrayl
 */
public class CustomInstanceRank {

	private int playerId;
	private int rank;
	private long lastEntry;
	private PersistentState state;

	public CustomInstanceRank(int playerId, int rank, long lastEntry, PersistentState state) {
		this.playerId = playerId;
		this.rank = rank;
		this.lastEntry = lastEntry;
		this.state = state;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
		state = PersistentState.UPDATE_REQUIRED;
	}

	public long getLastEntry() {
		return lastEntry;
	}

	public void setLastEntry(long lastEntry) {
		this.lastEntry = lastEntry;
		state = PersistentState.UPDATE_REQUIRED;
	}

	public PersistentState getPersistentState() {
		return state;
	}

	public void setPersistentState(PersistentState state) {
		this.state = state;
	}
}
