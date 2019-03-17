package com.aionemu.gameserver.custom.instance;

import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;

/**
 * @author Estrayl
 */
public class CustomInstanceRank {

	private int rank;
	private long lastEntry;
	private PersistentState state;

	public CustomInstanceRank(int rank, long lastEntry, PersistentState state) {
		this.rank = rank;
		this.lastEntry = lastEntry;
		this.state = state;
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
