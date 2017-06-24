package com.aionemu.gameserver.model.stats.container;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	protected boolean isDead = false;
	protected boolean isAboutToDie = false;// for long animation skills that will kill
	protected int killingBlow;// for long animation skills that will kill - last damage
	protected final T owner;
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
		int maxHp = getOwner().getGameStats().getMaxHp().getCurrent();
		if (maxHp == 0) {
			maxHp = 1;

			log.warn("CHECKPOINT: maxhp is 0 :" + this.getOwner().getSpawn().getNpcId());
		}
		return maxHp;
	}

	/**
	 * @return maxMp of creature according to stats
	 */
	public int getMaxMp() {
		return getOwner().getGameStats().getMaxMp().getCurrent();
	}

	/**
	 * @return the isDead There is no setter method cause life stats should be completely renewed on revive
	 */
	public boolean isDead() {
		return isDead;
	}

	public void setIsAboutToDie(boolean value) {
		this.isAboutToDie = value;
	}

	public boolean isAboutToDie() {
		return isAboutToDie;
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
	 * This method is called whenever caller wants to absorb creatures' HP
	 * 
	 * @param type
	 *          - attack type (see {@link SM_ATTACK_STATUS.TYPE}), if null, no {@link SM_ATTACK_STATUS} packet will be sent
	 * @param value
	 *          - hp to subtract
	 * @param skillId
	 *          - skillId (0 if none)
	 * @param log
	 *          - log type (see {@link SM_ATTACK_STATUS.LOG}) for the attack status packet to be sent
	 * @param attacker
	 *          - attacking creature or self
	 * @return The HP that this creature has left. If 0, the creature died.
	 */
	public int reduceHp(TYPE type, int value, int skillId, LOG log, Creature attacker) {
		Objects.requireNonNull(attacker, "attacker");

		if (getOwner().isInvulnerable())
			return currentHp;

		int hpReduced = 0;
		hpLock.lock();
		try {
			if (isDead)
				return 0;

			int newHp = Math.max(currentHp - value, 0);
			if (newHp < currentHp) {
				hpReduced = currentHp - newHp;
				currentHp = newHp;
				if (currentHp == 0) {
					currentMp = 0;
					isDead = true;
					unsetIsAboutToDie();
				}
			}
		} finally {
			hpLock.unlock();
		}

		if (hpReduced > 0 || skillId != 0)
			onReduceHp(type, hpReduced, skillId, log);
		if (hpReduced > 0) {
			if (isDead)
				getOwner().getController().onDie(attacker);
			getOwner().getObserveController().notifyHPChangeObservers(currentHp);
		}
		return currentHp;
	}

	/**
	 * This method is called whenever caller wants to absorb creatures's MP
	 * 
	 * @param type
	 *          - attack type (see {@link SM_ATTACK_STATUS.TYPE}), if null, no {@link SM_ATTACK_STATUS} packet will be sent
	 * @param value
	 *          - hp to subtract
	 * @param skillId
	 *          - skillId (0 if none)
	 * @param log
	 *          - log type (see {@link SM_ATTACK_STATUS.LOG}) for the attack status packet to be sent
	 * @return The MP that this creature has left.
	 */
	public int reduceMp(TYPE type, int value, int skillId, LOG log) {
		int mpReduced = 0;
		mpLock.lock();
		try {
			if (isDead)
				return 0;

			int newMp = Math.max(currentMp - value, 0);
			if (newMp < currentMp) {
				mpReduced = currentMp - newMp;
				currentMp = newMp;
			}
		} finally {
			mpLock.unlock();
		}

		if (mpReduced > 0 || skillId != 0)
			onReduceMp(type, mpReduced, skillId, log);
		return currentMp;
	}

	protected void sendAttackStatusPacketUpdate(TYPE type, int value, int skillId, LOG log) {
		if (type != null)
			PacketSendUtility.broadcastToSightedPlayers(owner, new SM_ATTACK_STATUS(owner, type, skillId, value, log), true);
	}

	/**
	 * This method is called whenever caller wants to restore creatures's HP
	 * 
	 * @param value
	 * @return currentHp
	 */
	public int increaseHp(int value) {
		return increaseHp(TYPE.REGULAR, value, 0, LOG.REGULAR);
	}

	public int increaseHp(TYPE type, int value, int skillId, LOG log) {
		if (getOwner().getEffectController().isAbnormalSet(AbnormalState.DISEASE))
			return currentHp;

		int hpIncreased;
		hpLock.lock();
		try {
			if (isDead)
				return 0;

			int newHp = Math.min(currentHp + value, getMaxHp());
			hpIncreased = newHp - currentHp;
			currentHp = newHp;
			if (hpIncreased < 0 && currentHp <= 0) { // some skills reduce hp via a negative heal (ghost absorption)
				currentHp = 0;
				isDead = true;
			}
		} finally {
			hpLock.unlock();
		}

		if (hpIncreased > 0 || skillId != 0)
			onIncreaseHp(type, hpIncreased, skillId, log);

		if (hpIncreased > 0) {
			if (killingBlow != 0 && currentHp > killingBlow)
				unsetIsAboutToDie();
			getOwner().getObserveController().notifyHPChangeObservers(currentHp);
		}
		return currentHp;
	}

	/**
	 * This method is called whenever caller wants to restore creatures's MP
	 * 
	 * @param value
	 * @return currentMp
	 */
	public int increaseMp(int value) {
		return increaseMp(null, value, 0, null);
	}

	public int increaseMp(TYPE type, int value, int skillId, LOG log) {
		int mpIncreased = 0;
		mpLock.lock();
		try {
			if (isDead)
				return 0;

			int newMp = Math.min(currentMp + value, getMaxMp());
			if (newMp > currentMp) {
				mpIncreased = newMp - currentMp;
				currentMp = newMp;
			}
		} finally {
			mpLock.unlock();
		}

		if (mpIncreased > 0 || skillId != 0)
			onIncreaseMp(type, mpIncreased, skillId, log);
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
			if (lifeRestoreTask == null && !isDead()) {
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
		currentHp = getMaxHp();
		currentMp = getMaxMp();
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

	protected void onIncreaseHp(TYPE type, int value, int skillId, LOG log) {
		sendAttackStatusPacketUpdate(type, value, skillId, log);
	}

	protected void onReduceHp(TYPE type, int value, int skillId, LOG log) {
		sendAttackStatusPacketUpdate(type, value, skillId, log);
	}

	protected void onIncreaseMp(TYPE type, int value, int skillId, LOG log) {
	}

	protected void onReduceMp(TYPE type, int value, int skillId, LOG log) {
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
			currentHp = (int) (hpPercent / 100f * getMaxHp());
			getOwner().getObserveController().notifyHPChangeObservers(currentHp);
			if (currentHp > 0)
				isDead = false;
		} finally {
			hpLock.unlock();
		}
	}

	/**
	 * Sets the current HP without sending packets or notifying observers
	 */
	public final void setCurrentHp(int hp) {
		hpLock.lock();
		try {
			currentHp = Math.max(0, hp);
			isDead = currentHp == 0;
		} finally {
			hpLock.unlock();
		}
	}

	/**
	 * Sets the current MP without sending packets or notifying observers
	 */
	public final void setCurrentMp(int value) {
		mpLock.lock();
		try {
			currentMp = Math.max(0, value);
		} finally {
			mpLock.unlock();
		}
	}

	/**
	 * This method can be used for Npc's to fully restore its MP
	 * 
	 * @param mpPercent
	 */
	public final void setCurrentMpPercent(int mpPercent) {
		mpLock.lock();
		try {
			this.currentMp = (int) (mpPercent / 100f * getMaxMp());
		} finally {
			mpLock.unlock();
		}
	}

}
