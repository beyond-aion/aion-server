package com.aionemu.gameserver.services.instance.periodic;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.services.CronService;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_AUTO_GROUP;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author ViAl, Sykra
 */
public abstract class PeriodicInstance {

	protected static final Logger log = LoggerFactory.getLogger(PeriodicInstance.class);

	// required properties
	private final CronExpression[] startExpressions;
	protected final int[] maskIds;
	protected final byte minLevel;
	protected final byte maxLevel;
	protected final long registrationPeriod;

	// inner variables
	protected AtomicBoolean registrationRunning = new AtomicBoolean(false);
	private final List<Integer> playersWithCooldown = new CopyOnWriteArrayList<>();
	private Future<?> unregisterTask;

	public PeriodicInstance(CronExpression[] startExpressions, long regPeriod, int[] maskIds, byte minLevel, byte maxLevel) {
		this.startExpressions = startExpressions;
		this.registrationPeriod = regPeriod;
		this.maskIds = maskIds;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
	}

	public final void scheduleRegistrationIfEnabled() {
		for (CronExpression startExpression : startExpressions) {
			CronService.getInstance().schedule(this::startRegistration, startExpression);
			log.info("Scheduled " + getClass().getSimpleName() + ": based on cron expression: " + startExpression + " Duration: " + registrationPeriod
				+ " in minutes");
		}
	}

	public final void startRegistration() {
		if (!registrationRunning.compareAndSet(false, true)) {
			log.warn("Tried to register " + getClass().getSimpleName() + " while an registration period is active.");
			return;
		}
		// start unregister task
		unregisterTask = ThreadPoolManager.getInstance().schedule(this::stopRegistration, registrationPeriod * 60 * 1000);

		World.getInstance().forEachPlayer(player -> {
			if (checkPlayerLevel(player.getLevel()))
				for (int maskId : maskIds)
					sendEntry(player, maskId);
		});
	}

	protected void sendEntry(Player player, int maskId) {
		PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon));
	}

	public final void stopRegistration() {
		if (!registrationRunning.compareAndSet(true, false)) {
			log.warn("Tried to unregister " + getClass().getSimpleName() + " while there is no active period");
			return;
		}
		playersWithCooldown.clear();
		for (int maskId : maskIds)
			AutoGroupService.getInstance().unregisterInstance(maskId);
		World.getInstance().forEachPlayer(player -> {
			if (checkPlayerLevel(player.getLevel()))
				for (int maskId : maskIds)
					PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, true));
		});
		if (unregisterTask != null && !unregisterTask.isCancelled()) {
			unregisterTask.cancel(false);
			unregisterTask = null;
		}
	}

	public boolean isEnterAvailable(Player player) {
		return isRegisterAvailable() && checkPlayerLevel(player.getLevel());
	}

	private boolean checkPlayerLevel(byte playerLevel) {
		return playerLevel > minLevel && playerLevel <= maxLevel;
	}

	public final boolean isRegisterAvailable() {
		return registrationRunning.get();
	}

	public void addCooldown(Player player) {
		playersWithCooldown.add(player.getObjectId());
	}

	public boolean hasCooldown(Player player) {
		return playersWithCooldown.contains(player.getObjectId());
	}

	public void showWindow(Player player) {
		if (!hasCooldown(player))
			for (int maskId : maskIds)
				PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId));
	}

	public int[] getMaskIds() {
		return maskIds;
	}

	public byte getMinLevel() {
		return minLevel;
	}

	public byte getMaxLevel() {
		return maxLevel;
	}

}
