package com.aionemu.gameserver.model.stats.container;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.services.LifeStatsRestoreService;

/**
 * @author ATracer
 */
public class NpcLifeStats extends CreatureLifeStats<Npc> {

	/**
	 * @param owner
	 */
	public NpcLifeStats(Npc owner) {
		super(owner, owner.getGameStats().getMaxHp().getCurrent(), owner.getGameStats().getMaxMp().getCurrent());
	}

	@Override
	public void triggerRestoreTask() {
		restoreLock.lock();
		try {
			if (lifeRestoreTask == null && !alreadyDead) {
				this.lifeRestoreTask = LifeStatsRestoreService.getInstance().scheduleHpRestoreTask(this);
			}
		} finally {
			restoreLock.unlock();
		}
	}
}
