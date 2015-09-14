package com.aionemu.gameserver.model.stats.container;

import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantLock;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FLY_TIME;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATUPDATE_HP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATUPDATE_MP;
import com.aionemu.gameserver.services.LifeStatsRestoreService;
import com.aionemu.gameserver.taskmanager.tasks.TeamEffectUpdater;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, sphinx
 */
public class PlayerLifeStats extends CreatureLifeStats<Player> {

	protected int currentFp;
	private final ReentrantLock fpLock = new ReentrantLock();

	private Future<?> flyRestoreTask;
	private Future<?> flyReduceTask;

	public PlayerLifeStats(Player owner) {
		super(owner, owner.getGameStats().getMaxHp().getCurrent(), owner.getGameStats().getMaxMp().getCurrent());
		this.currentFp = owner.getGameStats().getFlyTime().getCurrent();
	}

	@Override
	protected void onReduceHp(TYPE type, int value, int skillId, LOG log) {
		sendAttackStatusPacketUpdate(type, value, skillId, log);
		sendHpPacketUpdate();
		triggerRestoreTask();
		sendGroupPacketUpdate();
	}

	@Override
	protected void onReduceMp(TYPE type, int value, int skillId, LOG log) {
		sendAttackStatusPacketUpdate(type, value, skillId, log);
		sendMpPacketUpdate();
		triggerRestoreTask();
		sendGroupPacketUpdate();
	}

	@Override
	protected void onIncreaseMp(TYPE type, int value, int skillId, LOG log) {
		if (value > 0) {
			sendMpPacketUpdate();
			sendAttackStatusPacketUpdate(type, value, skillId, log);
			sendGroupPacketUpdate();
		}
	}

	@Override
	protected void onIncreaseHp(TYPE type, int value, int skillId, LOG log) {
		if (this.isFullyRestoredHp()) {
			// FIXME: Temp Fix: Reset aggro list when hp is full.
			this.owner.getAggroList().clear();
		}
		if (value > 0 || type == TYPE.REGULAR) {
			sendHpPacketUpdate();
			sendAttackStatusPacketUpdate(type, value, skillId, log);
			sendGroupPacketUpdate();
		}
	}

	private void sendGroupPacketUpdate() {
		if (owner.isInTeam()) {
			TeamEffectUpdater.getInstance().startTask(owner);
		}
	}

	@Override
	public void synchronizeWithMaxStats() {
		if (isAlreadyDead())
			return;

		super.synchronizeWithMaxStats();
		int maxFp = getMaxFp();
		if (currentFp != maxFp)
			currentFp = maxFp;
	}

	@Override
	public void updateCurrentStats() {
		super.updateCurrentStats();

		if (!isFullyRestoredHpMp())
			triggerRestoreTask();

		if (getMaxFp() < currentFp)
			currentFp = getMaxFp();

		if (owner.getFlyState() == 0 && !owner.isInSprintMode())
			triggerFpRestore();
	}

	public void sendHpPacketUpdate() {
		PacketSendUtility.sendPacket(owner, new SM_STATUPDATE_HP(currentHp, getMaxHp()));
	}

	public void sendMpPacketUpdate() {
		PacketSendUtility.sendPacket(owner, new SM_STATUPDATE_MP(currentMp, getMaxMp()));
	}

	/**
	 * @return the currentFp
	 */
	@Override
	public int getCurrentFp() {
		return this.currentFp;
	}

	@Override
	public int getMaxFp() {
		return owner.getGameStats().getFlyTime().getCurrent();
	}

	/**
	 * @return FP percentage 0 - 100
	 */
	public int getFpPercentage() {
		return 100 * currentFp / getMaxFp();
	}

	/**
	 * This method is called whenever caller wants to restore creatures's FP
	 * 
	 * @param value
	 * @return
	 */
	@Override
	public int increaseFp(int value) {
		return this.increaseFp(null, value, 0, null);
	}

	public int increaseFp(TYPE type, int value, int skillId, LOG log) {
		fpLock.lock();

		try {
			if (isAlreadyDead()) {
				return 0;
			}
			int newFp = this.currentFp + value;
			if (newFp > getMaxFp()) {
				newFp = getMaxFp();
				value = getMaxFp() - this.currentFp;
			}
			if (currentFp != newFp) {
				onIncreaseFp(type, value, skillId, log);
				this.currentFp = newFp;
			}
		} finally {
			fpLock.unlock();
		}

		return currentFp;

	}

	/**
	 * This method is called whenever caller wants to reduce creatures's MP
	 * 
	 * @param value
	 * @return
	 */
	public int reduceFp(int value) {
		return reduceFp(null, value, 0, null);
	}

	public int reduceFp(TYPE type, int value, int skillId, LOG log) {
		fpLock.lock();
		try {
			int newFp = this.currentFp - value;

			if (newFp < 0) {
				newFp = 0;
				value = this.currentFp;
			}

			this.currentFp = newFp;
		} finally {
			fpLock.unlock();
		}

		onReduceFp(type, value, skillId, log);

		return currentFp;
	}

	public int setCurrentFp(int value) {
		fpLock.lock();
		try {
			int newFp = value;

			if (newFp < 0)
				newFp = 0;

			this.currentFp = newFp;
		} finally {
			fpLock.unlock();
		}

		onReduceFp(null, value, 0, null);

		return currentFp;
	}

	protected void onIncreaseFp(TYPE type, int value, int skillId, LOG log) {
		if (value > 0) {
			sendAttackStatusPacketUpdate(type, value, skillId, log);
			sendFpPacketUpdate();
		}
	}

	protected void onReduceFp(TYPE type, int value, int skillId, LOG log) {
		sendAttackStatusPacketUpdate(type, value, skillId, log);
		sendFpPacketUpdate();
	}

	public void sendFpPacketUpdate() {
		if (owner == null)
			return;

		PacketSendUtility.sendPacket(owner, new SM_FLY_TIME(currentFp, getMaxFp()));
	}

	/**
	 * this method should be used only on FlyTimeRestoreService
	 */
	public void restoreFp() {
		// how much fly time restoring per 2 second.
		increaseFp(TYPE.NATURAL_FP, 1, 0, LOG.REGULAR);
	}

	public void specialrestoreFp() {
		if (owner.getGameStats().getStat(StatEnum.REGEN_FP, 0).getCurrent() != 0)
			increaseFp(TYPE.NATURAL_FP, owner.getGameStats().getStat(StatEnum.REGEN_FP, 0).getCurrent() / 3, 0, LOG.REGULAR);
	}

	public void triggerFpRestore() {
		cancelFpReduce();

		restoreLock.lock();
		try {
			if (flyRestoreTask == null && !alreadyDead && !isFlyTimeFullyRestored()) {
				this.flyRestoreTask = LifeStatsRestoreService.getInstance().scheduleFpRestoreTask(this);
			}
		} finally {
			restoreLock.unlock();
		}
	}

	public void cancelFpRestore() {
		restoreLock.lock();
		try {
			if (flyRestoreTask != null && !flyRestoreTask.isCancelled()) {
				flyRestoreTask.cancel(false);
				this.flyRestoreTask = null;
			}
		} finally {
			restoreLock.unlock();
		}
	}

	public void triggerFpReduceByCost(Integer costFp) {
		triggerFpReduce(costFp);
	}

	public void triggerFpReduce() {
		triggerFpReduce(null);
	}

	private void triggerFpReduce(Integer costFp) {
		cancelFpRestore();
		restoreLock.lock();
		try {
			if (flyReduceTask == null && !alreadyDead && owner.getAccessLevel() < AdminConfig.GM_FLIGHT_UNLIMITED && !owner.isUnderNoFPConsum()) {
				this.flyReduceTask = LifeStatsRestoreService.getInstance().scheduleFpReduceTask(this, costFp);
			}
		} finally {
			restoreLock.unlock();
		}
	}

	public void cancelFpReduce() {
		restoreLock.lock();
		try {
			if (flyReduceTask != null && !flyReduceTask.isCancelled()) {
				flyReduceTask.cancel(false);
				this.flyReduceTask = null;
			}
		} finally {
			restoreLock.unlock();
		}
	}

	public boolean isFlyTimeFullyRestored() {
		return getMaxFp() == currentFp;
	}

	@Override
	public void cancelAllTasks() {
		super.cancelAllTasks();
		cancelFpReduce();
		cancelFpRestore();
	}

	public void triggerRestoreOnRevive() {
		this.triggerRestoreTask();
		triggerFpRestore();
	}
}
