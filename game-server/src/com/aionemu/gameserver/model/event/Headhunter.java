package com.aionemu.gameserver.model.event;

import com.aionemu.gameserver.model.gameobjects.Persistable;

/**
 * Created on 30.05.2016
 * 
 * @author Estrayl
 * @since AION 4.8
 */
public class Headhunter implements Comparable<Headhunter>, Persistable {

	private PersistentState state;
	private final int hunterId;
	private int accumulatedKills;
	private long lastUpdate;

	public Headhunter(final int hunterId, final int accumulatedKills, final long lastUpdate, PersistentState state) {
		this.hunterId = hunterId;
		this.accumulatedKills = accumulatedKills;
		this.state = state;
		this.lastUpdate = lastUpdate;
	}

	public int getHunterId() {
		return hunterId;
	}

	public int getKills() {
		return accumulatedKills;
	}

	public void setKills(int accumulatedKills) {
		this.accumulatedKills = accumulatedKills;
	}

	public int incrementAndGetKills() {
		accumulatedKills++;
		lastUpdate = System.currentTimeMillis();
		state = PersistentState.UPDATE_REQUIRED;
		return accumulatedKills;
	}

	@Override
	public PersistentState getPersistentState() {
		return state;
	}

	@Override
	public void setPersistentState(PersistentState state) {
		this.state = state;
	}

	public long getLastUpdate() {
		return lastUpdate;
	}

	@Override
	public int compareTo(Headhunter hunter) {
		if (accumulatedKills > hunter.getKills())
			return -1;
		else if (accumulatedKills < hunter.getKills())
			return 1;
		else // accumulatedKills == hunter.getKills()
			return lastUpdate > hunter.getLastUpdate() ? -1 : 1;
	}
}
