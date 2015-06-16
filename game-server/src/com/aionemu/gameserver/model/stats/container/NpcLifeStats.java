package com.aionemu.gameserver.model.stats.container;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
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
	protected void onIncreaseHp(TYPE type, int value, int skillId, LOG log) {
		sendAttackStatusPacketUpdate(type, value, skillId, log);
	}

	@Override
	protected void onIncreaseMp(TYPE type, int value, int skillId, LOG log) {
		// nothing todo
	}

	@Override
	protected void onReduceHp(TYPE type, int value, int skillId, LOG log) {
		sendAttackStatusPacketUpdate(type, value, skillId, log);
	}

	@Override
	protected void onReduceMp(TYPE type, int value, int skillId, LOG log) {
		// nothing todo
	}

	@Override
	public void triggerRestoreTask() {
		restoreLock.lock();
		try {
			if (lifeRestoreTask == null && !alreadyDead) {
				this.lifeRestoreTask = LifeStatsRestoreService.getInstance().scheduleHpRestoreTask(this);
			}
		}
		finally {
			restoreLock.unlock();
		}
	}

	public void startResting() {
		triggerRestoreTask();
	}
}
