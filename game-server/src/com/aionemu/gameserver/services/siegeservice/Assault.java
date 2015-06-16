package com.aionemu.gameserver.services.siegeservice;

import java.util.concurrent.Future;

import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeRace;

/**
 * @author Luzien
 */
public abstract class Assault<siege extends Siege<?>> {
	
	protected final SiegeLocation siegeLocation;
	protected final int locationId;
	protected final SiegeNpc boss;
	protected final int worldId;

	protected Future<?> dredgionTask;
	protected Future<?> spawnTask;
	
	public Assault(Siege<?> siege) {
		this.siegeLocation = siege.getSiegeLocation();
		this.boss = siege.getBoss();
		this.locationId = siege.getSiegeLocationId();
		this.worldId = siege.getSiegeLocation().getWorldId();
	}
	
	public int getWorldId() {
		return worldId;
	}
	
	public void startAssault(int delay) {
		scheduleAssault(delay);
	}
	
	public void finishAssault(boolean captured) {
		if (dredgionTask != null && !dredgionTask.isDone())
			dredgionTask.cancel(true);
		if (spawnTask != null && !spawnTask.isDone())
			spawnTask.cancel(true);
		
		onAssaultFinish(captured && siegeLocation.getRace().equals(SiegeRace.BALAUR));
	}

	protected abstract void onAssaultFinish(boolean captured);

	protected abstract void scheduleAssault(int delay);

}