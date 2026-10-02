package com.aionemu.gameserver.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.dataholders.PvpExpTable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * Limits the PvP XP a player can gain: from the same victim only once per cooldown, and from all victims only while the accumulated PvP XP is
 * not above a cap. The accumulated PvP XP is stored with the player and decreases only while the player is online. Cooldowns are kept in
 * memory and survive a relog.
 * 
 * @author SVDNESS
 */
public class PvpExpLimitService {

	private static final long CLEANUP_INTERVAL_MILLIS = 3 * 60 * 1000;

	private final Map<Integer, PvpExpLimits> limitsByPlayer = new ConcurrentHashMap<>();

	public static PvpExpLimitService getInstance() {
		return SingletonHolder.instance;
	}

	private PvpExpLimitService() {
		ThreadPoolManager.getInstance().scheduleAtFixedRate(this::removeUnusedLimits, CLEANUP_INTERVAL_MILLIS, CLEANUP_INTERVAL_MILLIS);
	}

	/**
	 * Both limits are updated even if the other one blocks, so killing the same victim again restarts its cooldown.
	 * 
	 * @return True if the killer may gain the given PvP XP.
	 */
	public boolean tryGainPvpExp(Player killer, Player victim, long xp) {
		PvpExpTable table = DataManager.PVP_EXP_TABLE;
		PlayerCommonData pcd = killer.getCommonData();
		long now = System.currentTimeMillis();
		boolean belowCap, cooldownExpired;
		PvpExpLimits limits = limitsByPlayer.computeIfAbsent(killer.getObjectId(), _ -> new PvpExpLimits(now));
		synchronized (limits) {
			reduceAccumulatedXp(killer, limits, now);
			belowCap = pcd.getPvpExp() <= table.getMaxFromAllUser(killer.getLevel());
			if (belowCap)
				pcd.setPvpExp(pcd.getPvpExp() + xp);
			Long previousCooldownEnd = limits.cooldownEndByVictim.put(victim.getObjectId(), now + table.getDelayTimeMillis(killer.getLevel()));
			cooldownExpired = previousCooldownEnd == null || previousCooldownEnd < now;
		}
		if (!belowCap) // You cannot get any PVP XP for a while as you have gained too many PVP XP in too short a period of time.
			PacketSendUtility.sendPacket(killer, SM_SYSTEM_MESSAGE.STR_CANNOT_GET_PVP_EXP_TIMEBASE_LIMIT());
		if (!cooldownExpired) // You cannot get any PVP XP from the current target for a while.
			PacketSendUtility.sendPacket(killer, SM_SYSTEM_MESSAGE.STR_CANNOT_GET_PVP_EXP_TARGET_LIMIT());
		return belowCap && cooldownExpired;
	}

	public void onEnterWorld(Player player) {
		long now = System.currentTimeMillis();
		PvpExpLimits limits = limitsByPlayer.computeIfAbsent(player.getObjectId(), _ -> new PvpExpLimits(now));
		synchronized (limits) {
			limits.lastReduceTime = now;
		}
	}

	/**
	 * Applies the decrease of the accumulated PvP XP up to now, so the stored value is current.
	 */
	public void onLeaveWorld(Player player) {
		PvpExpLimits limits = limitsByPlayer.get(player.getObjectId());
		if (limits != null) {
			synchronized (limits) {
				reduceAccumulatedXp(player, limits, System.currentTimeMillis());
			}
		}
	}

	private void reduceAccumulatedXp(Player player, PvpExpLimits limits, long now) {
		PvpExpTable table = DataManager.PVP_EXP_TABLE;
		long intervalMillis = table.getReduceIntervalMillis(player.getLevel());
		if (intervalMillis <= 0)
			return;
		long intervals = (now - limits.lastReduceTime) / intervalMillis;
		if (intervals > 0) {
			PlayerCommonData pcd = player.getCommonData();
			pcd.setPvpExp(Math.max(0, pcd.getPvpExp() - intervals * table.getReduceAmount(player.getLevel())));
			limits.lastReduceTime += intervals * intervalMillis;
		}
	}

	private void removeUnusedLimits() {
		long now = System.currentTimeMillis();
		limitsByPlayer.entrySet().removeIf(e -> {
			PvpExpLimits limits = e.getValue();
			synchronized (limits) {
				limits.cooldownEndByVictim.values().removeIf(cooldownEnd -> cooldownEnd < now);
				return limits.cooldownEndByVictim.isEmpty() && World.getInstance().getPlayer(e.getKey()) == null;
			}
		});
	}

	private static class PvpExpLimits {

		private final Map<Integer, Long> cooldownEndByVictim = new HashMap<>();
		private long lastReduceTime;

		private PvpExpLimits(long now) {
			lastReduceTime = now;
		}
	}

	private static class SingletonHolder {

		private static final PvpExpLimitService instance = new PvpExpLimitService();
	}
}
