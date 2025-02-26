package com.aionemu.gameserver.model.stats.container;

import java.util.Objects;
import java.util.concurrent.Future;

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
	protected int killingBlow; // for long animation skills that will kill - last damage
	protected final T owner;
	private final Object hpLock = new Object();
	private final Object mpLock = new Object();
	protected final Object restoreLock = new Object();
	protected Future<?> lifeRestoreTask;

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

	public boolean isDead() {
		return currentHp == 0;
	}

	public boolean isAboutToDie() {
		return killingBlow != 0;
	}

	public void setKillingBlow(int killingBlow) {
		this.killingBlow = killingBlow;
	}

	private void unsetIsAboutToDie() {
		this.killingBlow = 0;
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
	 * @return The HP that this creature has left. If 0, the creature died.
	 */
	public int reduceHp(TYPE type, int value, int skillId, LOG log, Creature attacker) {
		Objects.requireNonNull(attacker, "attacker");
		if (getOwner().isInvulnerable()) {
			unsetIsAboutToDie();
			return currentHp;
		}

		int previousHp, newHp;
		synchronized (hpLock) {
			if (isDead())
				return 0;

			previousHp = currentHp;
			currentHp = newHp = Math.min(currentHp, Math.max(currentHp - value, 0));
			if (isDead()) {
				currentMp = 0;
				unsetIsAboutToDie();
			}
		}

		if (newHp != previousHp || skillId != 0)
			sendAttackStatusPacketUpdate(type, previousHp - newHp, skillId, log);
		if (newHp != previousHp)
			onHpChanged(previousHp, newHp, attacker);
		return newHp;
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
		int previousMp, newMp;
		synchronized (mpLock) {
			if (isDead())
				return 0;

			previousMp = currentMp;
			currentMp = newMp = Math.min(currentMp, Math.max(currentMp - value, 0));
		}

		if (newMp != previousMp || skillId != 0)
			sendAttackStatusPacketUpdate(type, previousMp - newMp, skillId, log);
		if (newMp != previousMp)
			onMpChanged(previousMp, newMp);
		return newMp;
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
		if (value < 0) // some skills reduce hp via a negative heal (e.g. 3732 Spirit Absorption)
			return reduceHp(type, -value, skillId, log, effector);

		if (getOwner().getEffectController().isAbnormalSet(AbnormalState.DISEASE))
			return currentHp;

		int previousHp, newHp;
		synchronized (hpLock) {
			if (isDead())
				return 0;

			previousHp = currentHp;
			currentHp = newHp = Math.min(currentHp + value, getMaxHp());
			if (killingBlow != 0 && newHp > killingBlow)
				unsetIsAboutToDie();
		}

		if (newHp != previousHp || skillId != 0)
			sendAttackStatusPacketUpdate(type, newHp - previousHp, skillId, log);
		if (newHp != previousHp)
			onHpChanged(previousHp, newHp, effector == null ? getOwner() : effector);
		return newHp;
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
		int previousMp, newMp;
		synchronized (mpLock) {
			if (isDead())
				return 0;

			previousMp = currentMp;
			currentMp = newMp = Math.max(currentMp, Math.min(currentMp + value, getMaxMp()));
		}

		if (newMp != previousMp || skillId != 0)
			sendAttackStatusPacketUpdate(type, newMp - previousMp, skillId, log);
		if (newMp != previousMp)
			onMpChanged(previousMp, newMp);
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
		synchronized (restoreLock) {
			if (lifeRestoreTask == null && !isDead()) {
				lifeRestoreTask = LifeStatsRestoreService.getInstance().scheduleRestoreTask(this);
			}
		}

	}

	/**
	 * Cancel currently running restore task
	 */
	public void cancelRestoreTask() {
		synchronized (restoreLock) {
			if (lifeRestoreTask != null) {
				lifeRestoreTask.cancel(false);
				lifeRestoreTask = null;
			}
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

	protected void onHpChanged(int previousHp, int newHp, Creature effector) {
		if (newHp == 0)
			getOwner().getController().onDie(effector);
		getOwner().getObserveController().notifyHPChangeObservers(newHp);
	}

	protected void onMpChanged(int previousMp, int newMp) {
	}

	public int getMaxFp() {
		return 0;
	}

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
		setCurrentHp((int) ((long) getMaxHp() * hpPercent / 100));
	}

	/**
	 * Sets the current HP without notifying observers
	 */
	public final void setCurrentHp(int hp) {
		setCurrentHp(hp, owner);
	}

	public final void setCurrentHp(int hp, Creature effector) {
		int previousHp, newHp;
		synchronized (hpLock) {
			previousHp = currentHp;
			currentHp = newHp = Math.max(0, Math.min(hp, getMaxHp()));
			if (killingBlow != 0 && (newHp == 0 || newHp > killingBlow))
				unsetIsAboutToDie();
		}
		if (newHp != previousHp) {
			// broadcast current hp percentage to others
			PacketSendUtility.broadcastToSightedPlayers(owner, new SM_ATTACK_STATUS(owner, TYPE.HP, 0, 0, LOG.REGULAR));
			onHpChanged(previousHp, newHp, effector);
		}
	}

	public final void setCurrentMp(int value) {
		int previousMp, newMp;
		synchronized (mpLock) {
			if (isDead())
				return;
			previousMp = currentMp;
			currentMp = newMp = Math.max(0, Math.min(value, getMaxMp()));
		}
		if (newMp != previousMp) {
			PacketSendUtility.broadcastToSightedPlayers(owner, new SM_ATTACK_STATUS(owner, TYPE.HEAL_MP, 0, 0, LOG.MPHEAL));
			onMpChanged(previousMp, newMp);
		}
	}

	/**
	 * This method can be used to fully restore owners MP
	 */
	public final void setCurrentMpPercent(int mpPercent) {
		setCurrentMp((int) ((long) getMaxMp() * mpPercent / 100));
	}

}
