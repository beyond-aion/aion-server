package com.aionemu.gameserver.services.instance.periodic;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

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
 * @author ViAl
 */
public abstract class PeriodicInstance {

	protected static final Logger log = LoggerFactory.getLogger(PeriodicInstance.class);
	// required properties
	protected boolean isEnabled;
	protected byte[] maskIds;
	protected byte minLevel = 45;
	protected byte maxLevel = 66;
	protected CronExpression startExpression;
	protected long registrationPeriod;
	// inner variables
	protected boolean registerAvailable;
	protected List<Integer> playersWithCooldown;
	protected Future<?> unregisterTask;

	public PeriodicInstance(boolean isEnabled, CronExpression startExpression, long regPeriod, byte[] maskIds, byte minLevel, byte maxLevel) {
		this.isEnabled = isEnabled;
		this.startExpression = startExpression;
		this.registrationPeriod = regPeriod;
		this.maskIds = maskIds;
		this.minLevel = minLevel;
		this.maxLevel = maxLevel;
		this.registerAvailable = false;
		this.playersWithCooldown = new ArrayList<>();
	}

	public void initIfEnabled() {
		if (isEnabled) {
			CronService.getInstance().schedule(() -> startRegistration(), startExpression);
			log.info("Scheduled " + this.getClass().getSimpleName() + ": based on cron expression: " + startExpression + " Duration: " + registrationPeriod
				+ " in minutes");
		}
	}

	public void startRegistration() {
		this.registerAvailable = true;
		startUnregisterTask();
		World.getInstance().forEachPlayer(player -> {
			if (player.getLevel() > minLevel && player.getLevel() <= maxLevel) {
				for (byte maskId : this.maskIds) {
					PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon));
					onSendEntry(player, maskId);
				}
			}
		});
	}

	protected void onSendEntry(Player player, byte maskId) {
	}

	public void stopRegistration() {
		registerAvailable = false;
		playersWithCooldown.clear();
		for (byte maskId : this.maskIds)
			AutoGroupService.getInstance().unRegisterInstance(maskId);
		World.getInstance().forEachPlayer(player -> {
			if (player.getLevel() > minLevel && player.getLevel() <= maxLevel) {
				for (byte maskId : this.maskIds)
					PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId, SM_AUTO_GROUP.wnd_EntryIcon, true));
			}
		});
		if (this.unregisterTask != null) {
			this.unregisterTask.cancel(false);
			this.unregisterTask = null;
		}
	}

	public boolean isEnterAvailable(Player player) {
		return registerAvailable && player.getLevel() > minLevel && player.getLevel() <= maxLevel;
	}

	public boolean isRegisterAvailable() {
		return registerAvailable;
	}

	public void addCooldown(Player player) {
		this.playersWithCooldown.add(player.getObjectId());
	}

	public boolean hasCooldown(Player player) {
		return this.playersWithCooldown.contains(player.getObjectId());
	}

	public void showWindow(Player player) {
		if (!this.playersWithCooldown.contains(player.getObjectId())) {
			for (byte maskId : this.maskIds)
				PacketSendUtility.sendPacket(player, new SM_AUTO_GROUP(maskId));
		}
	}

	public byte[] getMaskIds() {
		return maskIds;
	}

	public byte getMinLevel() {
		return minLevel;
	}

	public byte getMaxLevel() {
		return maxLevel;
	}

	protected void startUnregisterTask() {
		this.unregisterTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				stopRegistration();
			}
		}, this.registrationPeriod * 60 * 1000);
	}

}
