package com.aionemu.gameserver.services.siege;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.siege.SiegeNpc;
import com.aionemu.gameserver.model.siege.Assaulter;
import com.aionemu.gameserver.model.siege.SiegeLocation;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Luzien, Estrayl
 */
public abstract class Assault<SiegeType extends Siege<?>> {

	private final AtomicBoolean isStarted = new AtomicBoolean();
	protected final SiegeLocation siegeLocation;
	protected final SiegeNpc boss;
	protected final int locationId;
	protected final int worldId;

	protected Future<?> dredgionTask, spawnTask;

	public Assault(SiegeType siege) {
		this.siegeLocation = siege.getSiegeLocation();
		this.boss = siege.getBoss();
		this.locationId = siege.getSiegeLocationId();
		this.worldId = siege.getSiegeLocation().getWorldId();
	}

	public int getWorldId() {
		return worldId;
	}

	public void startAssault(int delay) {
		if (isStarted.compareAndSet(false, true))
			dredgionTask = ThreadPoolManager.getInstance().schedule(this::handleAssault, delay, TimeUnit.SECONDS);
	}

	public void finishAssault(boolean captured) {
		if (dredgionTask != null && !dredgionTask.isDone())
			dredgionTask.cancel(true);
		if (spawnTask != null && !spawnTask.isDone())
			spawnTask.cancel(true);

		onAssaultFinish(captured && siegeLocation.getRace() == SiegeRace.BALAUR);
	}

	protected abstract void onAssaultFinish(boolean captured);

	protected abstract void handleAssault();

	protected void spawnAssaulter(Assaulter a, SiegeNpc target) {
		int headingOffset = a.getHeadingOffset() * 10;
		float randomDirection = Rnd.get(-headingOffset, headingOffset) / 10f + target.getSpawn().getHeading();
		double radian = Math.toRadians(randomDirection * 3d);
		float x1 = (float) (target.getX() + Math.cos(radian) * a.getDistanceOffset());
		float y1 = (float) (target.getY() + Math.sin(radian) * a.getDistanceOffset());

		Npc spawned = (Npc) SpawnEngine.spawnObject(SpawnEngine.newSiegeSpawn(getWorldId(), a.getNpcId(), locationId, SiegeRace.BALAUR,
			SiegeModType.ASSAULT, x1, y1, target.getZ() + 0.5f, (byte) 0), 1);
		spawned.getAggroList().addHate(target, 100000);
	}

	protected String getBossNpcL10n() {
		if (boss != null && boss.getObjectTemplate() != null)
			return boss.getObjectTemplate().getL10n();
		return "";
	}

}
