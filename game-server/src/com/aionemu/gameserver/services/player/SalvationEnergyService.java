package com.aionemu.gameserver.services.player;

import java.sql.Timestamp;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.PlayerExperienceTable;
import com.aionemu.gameserver.dataholders.SalvationEnergyTable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * Gives Energy of Salvation to players who gain less XP on their level than the expected XP for their play time on it. Every check interval decides
 * whether the player can gain it, then it grows every bonus interval until the next check. A failed check removes it.
 */
public class SalvationEnergyService {

	/**
	 * Energy of Salvation is kept if the player logs in again within this time.
	 */
	public static final int RELOG_SECONDS = 600;
	private static final Map<Integer, Future<?>> tasks = new ConcurrentHashMap<>();

	public static void onEnterWorld(Player player) {
		player.getCommonData().startPlayTime();
		Ticker ticker = new Ticker(player);
		tasks.put(player.getObjectId(), ThreadPoolManager.getInstance().scheduleAtFixedRate(ticker, 0, 60000));
	}

	/**
	 * Starts a new check interval at once, as after login.
	 */
	public static void restart(Player player) {
		Future<?> task = tasks.put(player.getObjectId(), ThreadPoolManager.getInstance().scheduleAtFixedRate(new Ticker(player), 0, 60000));
		if (task != null)
			task.cancel(false);
	}

	public static void onLeaveWorld(Player player) {
		Future<?> task = tasks.remove(player.getObjectId());
		if (task != null)
			task.cancel(false);
		player.getCommonData().stopPlayTime();
	}

	private static class Ticker implements Runnable {

		private final Player player;
		private int minutesSinceCheck;
		private boolean canGain;

		private Ticker(Player player) {
			this.player = player;
		}

		@Override
		public void run() {
			PlayerCommonData pcd = player.getCommonData();
			int level = pcd.getLevel();
			SalvationEnergyTable table = DataManager.SALVATION_ENERGY_TABLE;
			if (minutesSinceCheck == 0 || minutesSinceCheck >= table.getCheckInterval(level)) {
				minutesSinceCheck = 0;
				canGain = isBehindExpectedExp(pcd);
				if (canGain) {
					gain(pcd);
				} else if (pcd.getSalvationEnergy() > 0 || pcd.getSalvationDropBonus() > 0) {
					pcd.resetSalvationPoints();
					pcd.setSalvationDropBonus(0);
					sendStats();
				}
			} else if (canGain && minutesSinceCheck % table.getBonusInterval(level) == 0) {
				gain(pcd);
			}
			minutesSinceCheck++;
		}

		private boolean isBehindExpectedExp(PlayerCommonData pcd) {
			int level = pcd.getLevel();
			SalvationEnergyTable table = DataManager.SALVATION_ENERGY_TABLE;
			if (table.getUseRatio(level) <= 0 || level >= DataManager.PLAYER_EXPERIENCE_TABLE.getMaxLevel() - 1)
				return false;
			if (pcd.getPlayTime() < table.getStandardPlayTime(level))
				return false;
			return hasPlayedLongEnoughOnLevel(pcd) && getMissingExp(pcd) >= getExpGainedOnLevel(pcd);
		}

		private void gain(PlayerCommonData pcd) {
			if (player.isDead() || hasRecentlyRelogged(pcd))
				return;
			int level = pcd.getLevel();
			SalvationEnergyTable table = DataManager.SALVATION_ENERGY_TABLE;
			long maxEnergy = table.getBonusPointsPerMinute(level) * table.getCheckInterval(level);
			if (pcd.getSalvationEnergy() >= maxEnergy) {
				pcd.setCurrentSalvationPoints(maxEnergy);
				return;
			}
			if (!hasPlayedLongEnoughOnLevel(pcd))
				return;
			long missingExp = getMissingExp(pcd);
			long gainedExp = getExpGainedOnLevel(pcd);
			if (missingExp < gainedExp)
				return;
			long energy = Math.min(table.getBonusPointsPerMinute(level) * table.getBonusInterval(level), missingExp - gainedExp - pcd.getSalvationEnergy());
			boolean hadEnergy = pcd.getSalvationEnergy() > 0;
			if (energy > 0)
				pcd.addSalvationPoints(energy);
			pcd.setSalvationDropBonus(table.getDropRatio(level));
			if (!hadEnergy)
				sendStats();
		}

		/**
		 * The client shows Energy of Salvation from the stats packet, so it must be resent when the energy appears or is removed.
		 */
		private void sendStats() {
			if (player.isSpawned())
				PacketSendUtility.sendPacket(player, new SM_STATS_INFO(player));
		}

		/**
		 * Energy of Salvation survives a relog within ten minutes, but does not grow during that time.
		 */
		private boolean hasRecentlyRelogged(PlayerCommonData pcd) {
			Timestamp lastOnline = pcd.getLastOnline();
			return lastOnline != null && System.currentTimeMillis() <= lastOnline.getTime() + RELOG_SECONDS * 1000;
		}

		private boolean hasPlayedLongEnoughOnLevel(PlayerCommonData pcd) {
			return pcd.getPlayTime() - pcd.getLevelUpPlayTime() >= DataManager.SALVATION_ENERGY_TABLE.getCheckStartTime(pcd.getLevel());
		}

		/**
		 * @return The XP expected for the play time on the current level, less the current Energy of Salvation.
		 */
		private long getMissingExp(PlayerCommonData pcd) {
			int playTimeOnLevel = pcd.getPlayTime() - pcd.getLevelUpPlayTime();
			return playTimeOnLevel * DataManager.SALVATION_ENERGY_TABLE.getStandardExpPerHour(pcd.getLevel()) / 60 - pcd.getSalvationEnergy();
		}

		private long getExpGainedOnLevel(PlayerCommonData pcd) {
			PlayerExperienceTable experienceTable = DataManager.PLAYER_EXPERIENCE_TABLE;
			return pcd.getExp() - experienceTable.getStartExpForLevel(pcd.getLevel());
		}
	}
}
