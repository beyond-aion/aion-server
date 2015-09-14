package com.aionemu.gameserver.model.stats.container;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.services.LifeStatsRestoreService;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public abstract class CreatureLifeStats<T extends Creature> {

	private static final Logger log = LoggerFactory.getLogger(CreatureLifeStats.class);
	protected int currentHp;
	protected int currentMp;
	protected boolean alreadyDead = false;
	protected boolean isAboutToDie = false;// for long animation skills that will kill
	protected int killingBlow;// for long animation skills that will kill - last damage
	protected T owner;
	private final Lock hpLock = new ReentrantLock();
	private final Lock mpLock = new ReentrantLock();
	protected final Lock restoreLock = new ReentrantLock();
	protected volatile Future<?> lifeRestoreTask;

	public CreatureLifeStats(T owner, int currentHp, int currentMp) {
		this.owner = owner;
		this.currentHp = currentHp;
		this.currentMp = currentMp;
	}

	public T getOwner() {
		return owner;
	}

	public int getCurrentHp() {
		return currentHp;
	}

	public int getCurrentMp() {
		return currentMp;
	}

	/**
	 * @return maxHp of creature according to stats
	 */
	public int getMaxHp() {
		int maxHp = this.getOwner().getGameStats().getMaxHp().getCurrent();
		if (maxHp == 0) {
			maxHp = 1;
			log.warn("CHECKPOINT: maxhp is 0 :" + this.getOwner().getGameStats());
		}
		return maxHp;
	}

	/**
	 * @return maxMp of creature according to stats
	 */
	public int getMaxMp() {
		return this.getOwner().getGameStats().getMaxMp().getCurrent();
	}

	/**
	 * @return the alreadyDead There is no setter method cause life stats should be completely renewed on revive
	 */
	public boolean isAlreadyDead() {
		return alreadyDead;
	}

	public void setIsAboutToDie(boolean value) {
		this.isAboutToDie = value;
	}

	public boolean isAboutToDie() {
		return this.isAboutToDie;
	}

	/**
	 * @return the killingBlow
	 */
	public int getKillingBlow() {
		return killingBlow;
	}

	/**
	 * @param killingBlow
	 *          the killingBlow to set
	 */
	public void setKillingBlow(int killingBlow) {
		this.killingBlow = killingBlow;
	}

	private void unsetIsAboutToDie() {
		this.isAboutToDie = false;
		this.killingBlow = 0;
	}

	/**
	 * This method is called whenever caller wants to absorb creatures's HP
	 * 
	 * @param value
	 * @param attacker
	 *          attacking creature or self
	 * @return currentHp
	 */
	public int reduceHp(int value, @Nonnull Creature attacker) {
		// this method doesnt send sm_attack_status packet
		return reduceHp(null, value, 0, null, attacker);
	}

	public int reduceHp(TYPE type, int value, int skillId, SM_ATTACK_STATUS.LOG log, @Nonnull Creature attacker) {
		Objects.requireNonNull(attacker, "attacker");

		boolean isDied = false;
		hpLock.lock();
		try {
			if (!alreadyDead) {
				int newHp = this.currentHp - value;

				if (newHp <= 0) {
					newHp = 0;
					this.currentMp = 0;
					alreadyDead = true;
					this.unsetIsAboutToDie();
					isDied = true;
					value = this.currentHp;
				}
				this.currentHp = newHp;
			}
		} finally {
			hpLock.unlock();
		}
		if (value != 0) {
			onReduceHp(type, value, skillId, log);
		}
		if (isDied) {
			getOwner().getController().onDie(attacker);
		}
		getOwner().getObserveController().notifyHPChangeObservers(currentHp);
		return currentHp;
	}

	/**
	 * This method is called whenever caller wants to absorb creatures's HP
	 * 
	 * @param value
	 * @return currentMp
	 */
	public int reduceMp(int value) {
		// this method doesnt send sm_attack_status packet
		return reduceMp(null, value, 0, null);
	}

	public int reduceMp(TYPE type, int value, int skillId, SM_ATTACK_STATUS.LOG log) {
		mpLock.lock();
		try {
			int newMp = this.currentMp - value;

			if (newMp < 0) {
				newMp = 0;
				value = this.currentMp;
			}

			this.currentMp = newMp;
		} finally {
			mpLock.unlock();
		}
		if (value != 0) {
			onReduceMp(type, value, skillId, log);
		}
		return currentMp;
	}

	protected void sendAttackStatusPacketUpdate(TYPE type, int value, int skillId, LOG log) {
		if (owner == null || type == null)
			return;
		PacketSendUtility.broadcastPacketAndReceive(owner, new SM_ATTACK_STATUS(owner, type, skillId, value, log));
	}

	/**
	 * This method is called whenever caller wants to restore creatures's HP
	 * 
	 * @param value
	 * @return currentHp
	 */
	public int increaseHp(int value) {
		return this.increaseHp(null, value, 0, null);
	}

	public int increaseHp(TYPE type, int value, int skillId, SM_ATTACK_STATUS.LOG log) {
		boolean hpIncreased = false;

		if (this.getOwner().getEffectController().isAbnormalSet(AbnormalState.DISEASE))
			return currentHp;

		hpLock.lock();
		try {
			if (isAlreadyDead()) {
				return 0;
			}
			int newHp = this.currentHp + value;
			if (newHp > getMaxHp()) {
				newHp = getMaxHp();
				value = getMaxHp() - this.currentHp;
			}
			if (currentHp != newHp) {
				this.currentHp = newHp;
				hpIncreased = true;
			}
		} finally {
			hpLock.unlock();
		}

		if (hpIncreased) {
			if (type == null)// just update packet
				onIncreaseHp(TYPE.REGULAR, 0, 0, LOG.REGULAR);
			else
				onIncreaseHp(type, value, skillId, log);

			if (this.killingBlow != 0 && this.currentHp > this.killingBlow)
				this.unsetIsAboutToDie();
		}
		getOwner().getObserveController().notifyHPChangeObservers(currentHp);
		return currentHp;
	}

	/**
	 * This method is called whenever caller wants to restore creatures's MP
	 * 
	 * @param value
	 * @return currentMp
	 */
	public int increaseMp(int value) {
		return this.increaseMp(null, value, 0, null);
	}

	public int increaseMp(TYPE type, int value, int skillId, SM_ATTACK_STATUS.LOG log) {
		boolean mpIncreased = false;
		mpLock.lock();
		try {
			if (isAlreadyDead()) {
				return 0;
			}
			int newMp = this.currentMp + value;
			if (newMp > getMaxMp()) {
				newMp = getMaxMp();
				value = getMaxMp() - this.currentMp;
			}
			if (currentMp != newMp) {
				this.currentMp = newMp;
				mpIncreased = true;
			}
		} finally {
			mpLock.unlock();
		}

		if (mpIncreased) {
			onIncreaseMp(type, value, skillId, log);
		}
		return currentMp;
	}

	/**
	 * Restores HP with value set as HP_RESTORE_TICK
	 */
	public final void restoreHp() {
		increaseHp(TYPE.NATURAL_HP, getOwner().getGameStats().getHpRegenRate().getCurrent(), 0, LOG.REGULAR);
	}

	/**
	 * Restores HP with value set as MP_RESTORE_TICK
	 */
	public final void restoreMp() {
		increaseMp(TYPE.NATURAL_MP, getOwner().getGameStats().getMpRegenRate().getCurrent(), 0, LOG.REGULAR);
	}

	/**
	 * Will trigger restore task if not already
	 */
	public void triggerRestoreTask() {
		restoreLock.lock();
		try {
			if (lifeRestoreTask == null && !isAlreadyDead()) {
				lifeRestoreTask = LifeStatsRestoreService.getInstance().scheduleRestoreTask(this);
			}
		} finally {
			restoreLock.unlock();
		}

	}

	/**
	 * Cancel currently running restore task
	 */
	public void cancelRestoreTask() {
		restoreLock.lock();
		try {
			if (lifeRestoreTask != null) {
				lifeRestoreTask.cancel(false);
				lifeRestoreTask = null;
			}
		} finally {
			restoreLock.unlock();
		}
	}

	/**
	 * @return true or false
	 */
	public boolean isFullyRestoredHpMp() {
		return getMaxHp() == currentHp && getMaxMp() == currentMp;
	}

	/**
	 * @return
	 */
	public boolean isFullyRestoredHp() {
		return getMaxHp() == currentHp;
	}

	public boolean isFullyRestoredMp() {
		return getMaxMp() == currentMp;
	}

	/**
	 * The purpose of this method is synchronize current HP and MP with updated MAXHP and MAXMP stats This method should be called only on creature load
	 * to game or player level up
	 */
	public void synchronizeWithMaxStats() {
		int maxHp = getMaxHp();
		if (currentHp != maxHp)
			currentHp = maxHp;
		int maxMp = getMaxMp();
		if (currentMp != maxMp)
			currentMp = maxMp;
	}

	/**
	 * The purpose of this method is synchronize current HP and MP with MAXHP and MAXMP when max stats were decreased below current level
	 */
	public void updateCurrentStats() {
		int maxHp = getMaxHp();
		if (maxHp < currentHp)
			currentHp = maxHp;

		int maxMp = getMaxMp();
		if (maxMp < currentMp)
			currentMp = maxMp;
	}

	/**
	 * @return HP percentage 0 - 100
	 */
	public int getHpPercentage() {
		return (int) (100f * currentHp / getMaxHp());
	}

	/**
	 * @return MP percentage 0 - 100
	 */
	public int getMpPercentage() {
		return (int) (100f * currentMp / getMaxMp());
	}

	protected abstract void onIncreaseMp(TYPE type, int value, int skillId, SM_ATTACK_STATUS.LOG log);

	protected abstract void onReduceMp(TYPE type, int value, int skillId, SM_ATTACK_STATUS.LOG log);

	protected abstract void onIncreaseHp(TYPE type, int value, int skillId, SM_ATTACK_STATUS.LOG log);

	protected abstract void onReduceHp(TYPE type, int value, int skillId, SM_ATTACK_STATUS.LOG log);

	/**
	 * @param type
	 * @param value
	 * @return
	 */
	public int increaseFp(int value) {
		return 0;
	}

	public int getMaxFp() {
		return 0;
	}

	/**
	 * @return
	 */
	public int getCurrentFp() {
		return 0;
	}

	/**
	 * Cancel all tasks when player logout
	 */
	public void cancelAllTasks() {
		cancelRestoreTask();
	}

	/**
	 * This method can be used for Npc's to fully restore its HP and remove dead state of lifestats
	 * 
	 * @param hpPercent
	 */
	public void setCurrentHpPercent(int hpPercent) {
		hpLock.lock();
		try {
			this.currentHp = (int) (hpPercent / 100f * getMaxHp());
			getOwner().getObserveController().notifyHPChangeObservers(currentHp);

			if (this.currentHp > 0) {
				this.alreadyDead = false;
				// just to be sure
				this.unsetIsAboutToDie();
			}
		} finally {
			hpLock.unlock();
		}
	}

	/**
	 * @param hp
	 */
	public void setCurrentHp(int hp) {
		boolean hpNotAtMaxValue = false;
		hpLock.lock();
		try {
			this.currentHp = hp;

			if (this.currentHp > 0) {
				this.alreadyDead = false;
				// just to be sure
				this.unsetIsAboutToDie();
			}

			if (this.currentHp < getMaxHp())
				hpNotAtMaxValue = true;
		} finally {
			hpLock.unlock();
		}
		if (hpNotAtMaxValue) {
			onReduceHp(SM_ATTACK_STATUS.TYPE.REGULAR, 0, 0, SM_ATTACK_STATUS.LOG.REGULAR);
		}
	}

	public int setCurrentMp(int value) {
		mpLock.lock();
		try {
			int newMp = value;

			if (newMp < 0)
				newMp = 0;

			this.currentMp = newMp;
		} finally {
			mpLock.unlock();
		}
		onReduceMp(SM_ATTACK_STATUS.TYPE.MP, 0, 0, SM_ATTACK_STATUS.LOG.REGULAR);
		return currentMp;
	}

	/**
	 * This method can be used for Npc's to fully restore its MP
	 * 
	 * @param mpPercent
	 */
	public void setCurrentMpPercent(int mpPercent) {
		mpLock.lock();
		try {
			this.currentMp = (int) (mpPercent / 100f * getMaxMp());
		} finally {
			mpLock.unlock();
		}
	}

}
