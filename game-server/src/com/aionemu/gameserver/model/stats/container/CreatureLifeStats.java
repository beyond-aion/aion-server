package com.aionemu.gameserver.model.stats.container;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.services.LifeStatsRestoreService;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public abstract class CreatureLifeStats<T extends Creature> {

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

	public int getMaxHp() {
		return getOwner().getGameStats().getMaxHp().getCurrent();
	}

	public int getMaxMp() {
		return getOwner().getGameStats().getMaxMp().getCurrent();
	}

	/**
	 * @return the isDead There is no setter method cause life stats should be completely renewed on revive
	 */
	public boolean isDead() {
		return isDead;
	}

	public void setIsAboutToDie() {
		this.isAboutToDie = true;
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

	public int reduceHp(TYPE type, int value, int skillId, LOG log, Creature attacker) {
		return reduceHp(type, value, skillId, log, attacker, true);
	}

	/**
	 * This method is called whenever caller wants to absorb creatures' HP
	 * 
	 * @param type
	 *          attack type (see {@link SM_ATTACK_STATUS.TYPE}), if null, no {@link SM_ATTACK_STATUS} packet will be sent
	 * @param value
	 *          hp to subtract
	 * @param skillId
	 *          skillId (0 if none)
	 * @param log
	 *          log type (see {@link SM_ATTACK_STATUS.LOG}) for the attack status packet to be sent
	 * @param attacker
	 *          attacking creature or self
	 * @param sendDiePacket
	 *          send SM_DIE to players
	 * @return The HP that this creature has left. If 0, the creature died.
	 */
	public int reduceHp(TYPE type, int value, int skillId, LOG log, Creature attacker, boolean sendDiePacket) {
		Objects.requireNonNull(attacker, "attacker");
		if (getOwner().isInvulnerable()) {
			if (isAboutToDie())
				unsetIsAboutToDie();
			return currentHp;
		}

		int hpReduced = 0;
		boolean died = false;
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
					setIsDead(died = true);
				}
			}
		} finally {
			hpLock.unlock();
		}

		if (hpReduced > 0 || skillId != 0)
			onReduceHp(type, hpReduced, skillId, log);
		if (died)
			getOwner().getController().onDie(attacker, sendDiePacket);
		if (hpReduced > 0)
			getOwner().getObserveController().notifyHPChangeObservers(currentHp);
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
	 * @return currentHp
	 */
	public int increaseHp(TYPE type, int value) {
		return increaseHp(type, value, getOwner(), 0, LOG.REGULAR);
	}

	public int increaseHp(TYPE type, int value, Creature effector) {
		return increaseHp(type, value, effector, 0, LOG.REGULAR);
	}

	public int increaseHp(TYPE type, int value, Effect effect, LOG log) {
		return increaseHp(type, value, effect.getEffector(), effect.getSkillId(), log);
	}

	private int increaseHp(TYPE type, int value, Creature effector, int skillId, LOG log) {
		if (getOwner().getEffectController().isAbnormalSet(AbnormalState.DISEASE))
			return currentHp;

		int hpIncreased;
		boolean died = false;
		hpLock.lock();
		try {
			if (isDead)
				return 0;

			int newHp = Math.min(currentHp + value, getMaxHp());
			hpIncreased = newHp - currentHp;
			currentHp = newHp;
			if (hpIncreased < 0 && currentHp <= 0) { // some skills reduce hp via a negative heal (ghost absorption)
				currentHp = 0;
				setIsDead(died = true);
			}
		} finally {
			hpLock.unlock();
		}

		if (hpIncreased > 0 || skillId != 0)
			onIncreaseHp(type, hpIncreased, skillId, log);
		if (died)
			getOwner().getController().onDie(effector == null ? getOwner() : effector, true);
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
		increaseHp(TYPE.NATURAL_HP, getOwner().getGameStats().getHpRegenRate().getCurrent());
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
		if (value > 0)
			onHpChanged();
	}

	protected void onReduceHp(TYPE type, int value, int skillId, LOG log) {
		sendAttackStatusPacketUpdate(type, value, skillId, log);
		if (value > 0)
			onHpChanged();
	}

	protected void onIncreaseMp(TYPE type, int value, int skillId, LOG log) {
		sendAttackStatusPacketUpdate(type, value, skillId, log);
		if (value > 0)
			onMpChanged();
	}

	protected void onReduceMp(TYPE type, int value, int skillId, LOG log) {
		sendAttackStatusPacketUpdate(type, value, skillId, log);
		if (value > 0)
			onMpChanged();
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
	 * This method can be used to fully restore owners HP and remove dead state of lifestats
	 */
	public void setCurrentHpPercent(int hpPercent) {
		setCurrentHp((int) (hpPercent / 100f * getMaxHp()));
	}

	/**
	 * Sets the current HP without notifying observers
	 */
	public final void setCurrentHp(int hp) {
		setCurrentHp(hp, owner);
	}

	public final void setCurrentHp(int hp, Creature effector) {
		hpLock.lock();
		boolean wasDead = isDead;
		int prevHp = currentHp;
		try {
			currentHp = Math.max(0, Math.min(hp, getMaxHp()));
			setIsDead(currentHp == 0);
		} finally {
			hpLock.unlock();
		}
		onSetHp();
		if (!wasDead && isDead)
			getOwner().getController().onDie(effector, true);
		if (prevHp != currentHp)
			getOwner().getObserveController().notifyHPChangeObservers(currentHp);
	}

	private void onSetHp() {
		// broadcast current hp percentage to others
		PacketSendUtility.broadcastToSightedPlayers(owner, new SM_ATTACK_STATUS(owner, TYPE.HP, 0, 0, LOG.REGULAR));
		// update hp bar on owners client
		onHpChanged();
	}

	protected void onHpChanged() {
	}

	/**
	 * Sets the current MP without notifying observers
	 */
	public final void setCurrentMp(int value) {
		mpLock.lock();
		try {
			currentMp = Math.max(0, Math.min(value, getMaxMp()));
		} finally {
			mpLock.unlock();
		}
		onSetMp();
	}

	/**
	 * This method can be used to fully restore owners MP
	 * 
	 * @param mpPercent
	 */
	public final void setCurrentMpPercent(int mpPercent) {
		mpLock.lock();
		try {
			currentMp = (int) (mpPercent / 100f * getMaxMp());
		} finally {
			mpLock.unlock();
		}
		onSetMp();
	}

	private void onSetMp() {
		// broadcast current mp percentage to others
		PacketSendUtility.broadcastToSightedPlayers(owner, new SM_ATTACK_STATUS(owner, TYPE.HEAL_MP, 0, 0, LOG.MPHEAL));
		// update mp bar on owners client
		onMpChanged();
	}

	protected void onMpChanged() {
	}

	private void setIsDead(boolean isDead) {
		if (this.isDead != isDead)
			unsetIsAboutToDie();
		this.isDead = isDead;
	}
}
