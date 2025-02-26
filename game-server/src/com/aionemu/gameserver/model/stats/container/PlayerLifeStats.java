package com.aionemu.gameserver.model.stats.container;

import java.util.concurrent.Future;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FLY_TIME;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATUPDATE_HP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATUPDATE_MP;
import com.aionemu.gameserver.services.LifeStatsRestoreService;
import com.aionemu.gameserver.taskmanager.tasks.TeamStatUpdater;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer, sphinx
 */
public class PlayerLifeStats extends CreatureLifeStats<Player> {

	private final Object fpLock = new Object();
	private int flightReducePeriod = 2;
	private int flightReduceValue = 1;
	private int currentFp;
	private Future<?> flyRestoreTask;
	private Future<?> flyReduceTask;

	public PlayerLifeStats(Player owner) {
		super(owner, owner.getGameStats().getMaxHp().getCurrent(), owner.getGameStats().getMaxMp().getCurrent());
		this.currentFp = owner.getGameStats().getFlyTime().getCurrent();
	}

	@Override
	protected void onHpChanged(int previousHp, int newHp, Creature effector) {
		if (isFullyRestoredHp()) // FIXME: Temp Fix: Reset aggro list when hp is full
			owner.getAggroList().clear();
		if (owner.isSpawned()) {
			sendHpPacketUpdate();
			sendGroupPacketUpdate();
			if (previousHp == 0 || newHp < previousHp)
				triggerRestoreTask();
			if (previousHp == 0)
				triggerFpRestore();
		}
		super.onHpChanged(previousHp, newHp, effector);
	}

	@Override
	protected void onMpChanged(int previousMp, int newMp) {
		super.onMpChanged(previousMp, newMp);
		if (owner.isSpawned()) {
			sendMpPacketUpdate();
			sendGroupPacketUpdate();
			if (newMp < previousMp)
				triggerRestoreTask();
		}
	}

	private void sendGroupPacketUpdate() {
		if (owner.isInTeam() && !TeamStatUpdater.getInstance().hasTask(owner)) {
			TeamStatUpdater.getInstance().startTask(owner);
		}
	}

	@Override
	public void synchronizeWithMaxStats() {
		if (isDead())
			return;

		super.synchronizeWithMaxStats();
		currentFp = getMaxFp();

		if (owner.isSpawned()) {
			sendHpPacketUpdate();
			sendMpPacketUpdate();
			sendFpPacketUpdate();
		}
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

	private void sendHpPacketUpdate() {
		PacketSendUtility.sendPacket(owner, new SM_STATUPDATE_HP(currentHp, getMaxHp()));
	}

	private void sendMpPacketUpdate() {
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
	 * This method is called whenever caller wants to restore creatures' FP
	 * 
	 * @param value
	 * @return
	 */
	public int increaseFp(TYPE type, int value, int skillId, LOG log) {
		synchronized (fpLock) {
			if (isDead()) {
				return 0;
			}
			int newFp = this.currentFp + value;
			if (newFp > getMaxFp()) {
				newFp = getMaxFp();
				value = getMaxFp() - this.currentFp;
			}
			if (currentFp != newFp) {
				this.currentFp = newFp;
				onIncreaseFp(type, value, skillId, log);
			}
		}

		return currentFp;

	}

	/**
	 * This method is called whenever caller wants to reduce creatures' FP
	 * 
	 * @return Current flight points
	 */
	public int reduceFp(TYPE type, int value, int skillId, LOG log) {
		synchronized (fpLock) {
			int newFp = this.currentFp - value;

			if (newFp < 0) {
				newFp = 0;
				value = this.currentFp;
			}

			this.currentFp = newFp;
		}

		onReduceFp(type, value, skillId, log);

		return currentFp;
	}

	public int setCurrentFp(int value) {
		synchronized (fpLock) {
			int newFp = value;

			if (newFp < 0)
				newFp = 0;

			this.currentFp = newFp;
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
		PacketSendUtility.sendPacket(owner, new SM_FLY_TIME(currentFp, getMaxFp()));
	}

	/**
	 * this method should be used only on FlyTimeRestoreService
	 */
	public void restoreFp() {
		// how much fly time restoring per 6 second.
		increaseFp(TYPE.NATURAL_FP, 3, 0, LOG.REGULAR);
	}

	public void specialrestoreFp() {
		if (owner.getGameStats().getStat(StatEnum.REGEN_FP, 0).getCurrent() != 0)
			increaseFp(TYPE.NATURAL_FP, owner.getGameStats().getStat(StatEnum.REGEN_FP, 0).getCurrent() / 3, 0, LOG.REGULAR);
	}

	public void triggerFpRestore() {
		synchronized (restoreLock) {
			cancelFpReduce();
			if (flyRestoreTask == null && !isDead() && !isFlyTimeFullyRestored()) {
				flyRestoreTask = LifeStatsRestoreService.getInstance().scheduleFpRestoreTask(this);
			}
		}
	}

	public void cancelFpRestore() {
		synchronized (restoreLock) {
			if (flyRestoreTask != null && !flyRestoreTask.isCancelled()) {
				flyRestoreTask.cancel(false);
				flyRestoreTask = null;
			}
		}
	}

	public void triggerFpReduce() {
		if (owner.hasAccess(AdminConfig.UNLIMITED_FLIGHT_TIME) || isDead())
			return;
		synchronized (restoreLock) {
			if (owner.isInSprintMode()) {
				flightReduceValue = owner.ride.getCostFp();
				flightReducePeriod = 1;
			} else if (owner.isFlying()) {
				boolean isInFlyArea = owner.isInsideZoneType(ZoneType.FLY) && !owner.isInsideZoneType(ZoneType.NO_FLY);
				flightReduceValue = isInFlyArea ? 1 : 2;
				flightReducePeriod = isInFlyArea && owner.isInGlidingState() ? 2 : 1;
			} else {
				return;
			}
			cancelFpRestore();
			if (flyReduceTask == null && !isDead())
				flyReduceTask = LifeStatsRestoreService.getInstance().scheduleFpReduceTask(this);
		}
	}

	public void cancelFpReduce() {
		synchronized (restoreLock) {
			if (flyReduceTask != null && !flyReduceTask.isCancelled()) {
				flyReduceTask.cancel(false);
				flyReduceTask = null;
			}
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

	public int getFlightReducePeriod() {
		return flightReducePeriod;
	}

	public int getFlightReduceValue() {
		return flightReduceValue;
	}
}
