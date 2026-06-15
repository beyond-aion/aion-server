package com.aionemu.gameserver.services;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.stats.container.CreatureLifeStats;
import com.aionemu.gameserver.model.stats.container.PlayerLifeStats;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public class LifeStatsRestoreService {

	private static final int DEFAULT_DELAY = 6000;

	private static LifeStatsRestoreService instance = new LifeStatsRestoreService();

	/**
	 * HP and MP restoring task
	 */
	public Future<?> scheduleRestoreTask(CreatureLifeStats<? extends Creature> lifeStats) {
		return ThreadPoolManager.getInstance().scheduleAtFixedRate(new HpMpRestoreTask(lifeStats), 1700, DEFAULT_DELAY);
	}

	public Future<?> scheduleHpRestoreTask(CreatureLifeStats<? extends Creature> lifeStats) {
		return ThreadPoolManager.getInstance().scheduleAtFixedRate(new HpRestoreTask(lifeStats), 1700, DEFAULT_DELAY);
	}

	public Future<?> scheduleFpReduceTask(PlayerLifeStats lifeStats) {
		return ThreadPoolManager.getInstance().scheduleAtFixedRate(new FpReduceTask(lifeStats), 1000, 1000);
	}

	public Future<?> scheduleFpRestoreTask(PlayerLifeStats lifeStats) {
		return ThreadPoolManager.getInstance().scheduleAtFixedRate(new FpRestoreTask(lifeStats), 3000, DEFAULT_DELAY);
	}

	public static LifeStatsRestoreService getInstance() {
		return instance;
	}

	private static class HpRestoreTask implements Runnable {

		private CreatureLifeStats<?> lifeStats;

		private HpRestoreTask(CreatureLifeStats<?> lifeStats) {
			this.lifeStats = lifeStats;
		}

		@Override
		public void run() {
			if (lifeStats.isDead() || lifeStats.isFullyRestoredHp() || !lifeStats.getOwner().isInWorld() || lifeStats.getOwner().getAi().getState() == AIState.FIGHT) {
				lifeStats.cancelRestoreTask();
				lifeStats = null;
			} else {
				lifeStats.restoreHp();
			}
		}
	}

	private static class HpMpRestoreTask implements Runnable {

		private CreatureLifeStats<?> lifeStats;

		private HpMpRestoreTask(CreatureLifeStats<?> lifeStats) {
			this.lifeStats = lifeStats;
		}

		@Override
		public void run() {
			if (lifeStats.isDead() || lifeStats.isFullyRestoredHpMp() || !lifeStats.getOwner().isInWorld()) {
				lifeStats.cancelRestoreTask();
				lifeStats = null;
			} else {
				lifeStats.restoreHp();
				lifeStats.restoreMp();
			}
		}
	}

	private static class FpReduceTask implements Runnable {

		private PlayerLifeStats lifeStats;
		private int secondsElapsed = 0;

		private FpReduceTask(PlayerLifeStats lifeStats) {
			this.lifeStats = lifeStats;
		}

		@Override
		public void run() {
			if (lifeStats.isDead() || !lifeStats.getOwner().isSpawned()) {
				lifeStats.cancelFpReduce();
				lifeStats = null;
				return;
			} else if (secondsElapsed % lifeStats.getFlightReducePeriod() == 0) {
				lifeStats.reduceFp(null, lifeStats.getFlightReduceValue(), 0, null);
				lifeStats.specialrestoreFp();
				if (lifeStats.getCurrentFp() <= 0) {
					if (lifeStats.getOwner().isFlying()) {
						lifeStats.getOwner().getFlyController().endFly(true);
					} else {
						lifeStats.triggerFpRestore();
					}
				}
			}
			secondsElapsed++;
		}
	}

	private static class FpRestoreTask implements Runnable {

		private PlayerLifeStats lifeStats;

		private FpRestoreTask(PlayerLifeStats lifeStats) {
			this.lifeStats = lifeStats;
		}

		@Override
		public void run() {
			if (lifeStats.isDead() || lifeStats.isFlyTimeFullyRestored()) {
				lifeStats.cancelFpRestore();
				lifeStats = null;
			} else {
				lifeStats.restoreFp();
			}
		}
	}
}
