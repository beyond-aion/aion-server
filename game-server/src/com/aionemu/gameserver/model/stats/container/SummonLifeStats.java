package com.aionemu.gameserver.model.stats.container;

import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.services.LifeStatsRestoreService;

/**
 * @author ATracer
 */
public class SummonLifeStats extends CreatureLifeStats<Summon> {

	public SummonLifeStats(Summon owner) {
		super(owner, owner.getGameStats().getMaxHp().getCurrent(), owner.getGameStats().getMaxMp().getCurrent());
	}

	@Override
	public void triggerRestoreTask() {
		synchronized (restoreLock) {
			if (lifeRestoreTask == null && !isDead())
				lifeRestoreTask = LifeStatsRestoreService.getInstance().scheduleHpRestoreTask(this);
		}
	}
}
