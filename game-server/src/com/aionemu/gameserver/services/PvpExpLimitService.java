package com.aionemu.gameserver.services;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.PvpExpTable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author SVDNESS
 */
// Retail: PvP EXP gain limit based on multiple conditions.
public class PvpExpLimitService {
	private static final int TARGET_CLEANUP_THRESHOLD = 128;
	private final Map<Integer, ExpBucket> buckets = new ConcurrentHashMap<>();
	private final Map<Integer, Map<Integer, Long>> targetExpire = new ConcurrentHashMap<>();

	private PvpExpLimitService() {}

	// Checks the limits and returns the allowed PvP EXP.
	public int tryGainPvpExp(Player killer, Player victim, int requestedXp) {
		if (requestedXp <= 0) {
			return 0;
		}
		PvpExpTable table = DataManager.PVP_EXP_TABLE;
		if (table == null) {
			return requestedXp;
		}
		long now = System.currentTimeMillis();
		int killerLevel = killer.getLevel();
		// Both gates are checked independently, each updating its own limit.
		boolean timebaseAllowed = checkTimebaseLimit(killer, table, killerLevel, requestedXp, now);
		boolean targetAllowed = checkTargetLimit(killer, victim, table, killerLevel, now);
		return timebaseAllowed && targetAllowed ? requestedXp : 0;
	}

	// Accumulated PvP EXP from all targets is reduced by a timer; exceeding the limit blocks further EXP gain.
	private boolean checkTimebaseLimit(Player killer, PvpExpTable table, int killerLevel, int requestedXp, long now) {
		int cap = table.getMaxFromAllUser(killerLevel);
		if (cap <= 0) {
			return true;
		}
		ExpBucket bucket = buckets.computeIfAbsent(killer.getObjectId(), _ -> new ExpBucket(now));
		synchronized (bucket) {
			drainBucket(bucket, table, killerLevel, now);
			if (bucket.value > cap) {
				// You cannot get any PVP XP for a while as you have gained too many PVP XP in too short a period of time.
				PacketSendUtility.sendPacket(killer, SM_SYSTEM_MESSAGE.STR_CANNOT_GET_PVP_EXP_TIMEBASE_LIMIT());
				return false;
			}
			bucket.value += requestedXp;
		}
		return true;
	}

	// The accumulator is reduced by reduceAmount for each elapsed interval, but never below zero.
	private void drainBucket(ExpBucket bucket, PvpExpTable table, int killerLevel, long now) {
		long intervalMillis = table.getReduceIntervalMillis(killerLevel);
		if (intervalMillis <= 0) {
			return;
		}
		long elapsed = now - bucket.lastReduceTime;
		if (elapsed <= intervalMillis) {
			return;
		}
		long drained = elapsed / intervalMillis * table.getReduceAmount(killerLevel);
		bucket.value = Math.max(0, bucket.value - drained);
		bucket.lastReduceTime = now;
	}

	// The target marker is always updated, even for a blocked attempt — repeated kills extend the cooldown.
	private boolean checkTargetLimit(Player killer, Player victim, PvpExpTable table, int killerLevel, long now) {
		long delayMillis = table.getDelayTimeMillis(killerLevel);
		if (delayMillis <= 0) {
			return true;
		}
		Map<Integer, Long> byTarget = targetExpire.computeIfAbsent(killer.getObjectId(), _ -> new ConcurrentHashMap<>());
		Long previousExpire = byTarget.put(victim.getObjectId(), now + delayMillis);
		if (byTarget.size() > TARGET_CLEANUP_THRESHOLD) {
			byTarget.entrySet().removeIf(entry -> entry.getValue() < now);
		}
		if (previousExpire != null && previousExpire >= now) {
			// You cannot get any PVP XP from the current target for a while.
			PacketSendUtility.sendPacket(killer, SM_SYSTEM_MESSAGE.STR_CANNOT_GET_PVP_EXP_TARGET_LIMIT());
			return false;
		}
		return true;
	}

	private static class ExpBucket {
		private long value;
		private long lastReduceTime;

		private ExpBucket(long now) {
			this.lastReduceTime = now;
		}
	}

	public static PvpExpLimitService getInstance() {
		return SingletonHolder.instance;
	}

	private static class SingletonHolder {
		private static final PvpExpLimitService instance = new PvpExpLimitService();
	}
}