package com.aionemu.gameserver.model.stats.container;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.services.LifeStatsRestoreService;

/**
 * @author ATracer
 */
public class NpcLifeStats extends CreatureLifeStats<Npc> {

	public NpcLifeStats(Npc owner) {
		super(owner);
	}

	@Override
	public void triggerRestoreTask() {
		synchronized (restoreLock) {
			if (lifeRestoreTask == null && !isDead()) {
				this.lifeRestoreTask = LifeStatsRestoreService.getInstance().scheduleHpRestoreTask(this);
			}
		}
	}
}
